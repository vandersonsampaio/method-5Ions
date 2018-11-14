package core.semantic.sentimentanalysis.googlecloud;

import java.net.UnknownHostException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentence;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Document.Type;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import io.db.LoadDocuments;
import io.db.SaveDocuments;

public class SentimentAnalysis implements Runnable {

	private final int NUMBERTHREAD = 1;
	private JSONArray arr;
	private String host;
	private String databaseName;
	private String collectionName;
	
	public SentimentAnalysis(String host, String databaseName, String collectionName) {
		arr = null;
		this.host = host;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
	}

	public SentimentAnalysis(String host, String databaseName, String collectionName,
			JSONArray arr) {
		this.arr = arr;
		this.host = host;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
	}
	
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

				int i = 1;
				JSONArray arrSentences = new JSONArray();
				JSONObject objAux = null;

				json.put("magnitude", sentiment.getMagnitude());
				json.put("score", sentiment.getScore());

				for (Sentence sentence : response.getSentencesList()) {
					objAux = new JSONObject();

					objAux.put("number", i++);

					objAux.put("offset", sentence.getText().getBeginOffset());
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

	public boolean analyzeSentimentText() throws UnknownHostException{
		LoadDocuments ld = new LoadDocuments(host, databaseName, collectionName);

		JSONArray jarr = ld.findByQuery(new BasicDBObject().append("is_entitysentiment", "false"),  1);

		int length = jarr.size() / NUMBERTHREAD;

		for (int i = 0; i < NUMBERTHREAD; i++) {
			SentimentAnalysis sa = new SentimentAnalysis(host, databaseName, collectionName,
					(JSONArray) jarr.subList(length * i, i + 1 < NUMBERTHREAD ? length * (i + 1) : jarr.size()));

			(new Thread(sa)).start();
		}

		return true;
	}
	
	@Override
	public void run() {
		// preciso pegar o offset de cada sentença
		try {
			SaveDocuments sd = new SaveDocuments(host, databaseName, collectionName);

			for (int i = 0; i < arr.size(); i++) {
				JSONObject json = (JSONObject) arr.get(i);
				
				JSONObject sentiment = this.analyzeSentimentText(json.get("text").toString(),
						json.get("title").toString(), json.get("date_published").toString());
				
				JSONArray sentences = (JSONArray) sentiment.get("sentences");
				BasicDBList ltSentences = new BasicDBList();
				
				for(int j = 0; j < sentences.size(); j++){
					ltSentences.add(new BasicDBObject().append("number_sentence", ((JSONObject)sentences.get(j)).get("number"))
							.append("score", ((JSONObject)sentences.get(j)).get("score"))
							.append("magnitude", ((JSONObject)sentences.get(j)).get("magnitude"))
							.append("offset", ((JSONObject)sentences.get(j)).get("offset")));
				}
				
				
				sd.updateDocument(new BasicDBObject().append("$set", new BasicDBObject().append("is_entitysentiment", "true")),
						new BasicDBObject().append("_id", ((JSONObject) arr.get(i)).get("_id").toString()));
				
				//Sentimento geral do documento
				sd.updateDocument(new BasicDBObject().append("$set", new BasicDBObject().append("score_sentiment", sentiment.get("score"))),
						new BasicDBObject().append("_id", ((JSONObject) arr.get(i)).get("_id").toString()));
				
				//Magnitude do documento
				sd.updateDocument(new BasicDBObject().append("$set", new BasicDBObject().append("score_sentiment", sentiment.get("magnitude"))),
						new BasicDBObject().append("_id", ((JSONObject) arr.get(i)).get("_id").toString()));
				
				//Sentimento de cada uma das sentenças
				sd.updateDocument(new BasicDBObject().append("$set", new BasicDBObject().append("sentiments", ltSentences)),
						new BasicDBObject().append("_id", ((JSONObject) arr.get(i)).get("_id").toString()));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
