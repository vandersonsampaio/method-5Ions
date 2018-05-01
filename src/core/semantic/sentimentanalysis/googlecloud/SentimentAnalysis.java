package core.semantic.sentimentanalysis.googlecloud;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentence;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Document.Type;

public class SentimentAnalysis {

	@SuppressWarnings("unchecked")
	public JSONObject analyzeSentimentText(String text, String tittle, String date) throws Exception {
		JSONObject json = new JSONObject();

		try (LanguageServiceClient language = LanguageServiceClient.create()) {
			Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
			AnalyzeSentimentResponse response = language.analyzeSentiment(doc);
			Sentiment sentiment = response.getDocumentSentiment();
			if (sentiment == null) {
				return null;
			} else {
				
				json.put("date", date);
				//json.put("tittle", tittle);
				
				int i = 1;
				JSONArray arrSentences = new JSONArray();
				JSONObject objAux = null;

				json.put("magnitude", sentiment.getMagnitude());
				json.put("score", sentiment.getScore());

				for (Sentence sentence : response.getSentencesList()) {
					objAux = new JSONObject();

					objAux.put("number", i++);

					if (sentence.getSentiment() == null) {
						objAux.put("score", 0);
						objAux.put("magnitude", 0);
					} else {
						objAux.put("score", sentence.getSentiment().getScore());
						objAux.put("magnitude", sentence.getSentiment().getMagnitude());

					}

					arrSentences.add(objAux);
				}
				json.put("sentences", arrSentences);

			}
			return json;
		}
	}

	/**
	 * Gets {@link Sentiment} from the contents of the GCS hosted file.
	 */
	public static Sentiment analyzeSentimentFile(String gcsUri) throws Exception {
		// [START analyze_sentiment_file]
		// Instantiate the Language client
		// com.google.cloud.language.v1.LanguageServiceClient
		try (LanguageServiceClient language = LanguageServiceClient.create()) {
			Document doc = Document.newBuilder().setGcsContentUri(gcsUri).setType(Type.PLAIN_TEXT).build();
			AnalyzeSentimentResponse response = language.analyzeSentiment(doc);
			Sentiment sentiment = response.getDocumentSentiment();
			if (sentiment == null) {
				System.out.println("No sentiment found");
			} else {
				System.out.printf("Sentiment magnitude : %.3f\n", sentiment.getMagnitude());
				System.out.printf("Sentiment score : %.3f\n", sentiment.getScore());
			}
			return sentiment;
		}
		// [END analyze_sentiment_file]
	}
}
