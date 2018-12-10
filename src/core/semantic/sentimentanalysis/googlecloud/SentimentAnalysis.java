package core.semantic.sentimentanalysis.googlecloud;

import java.net.UnknownHostException;
import java.util.List;

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

	private final int NUMBERTHREAD = 4;
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
				int beginOffset = 0;
				JSONArray arrSentences = new JSONArray();
				JSONObject objAux = null;

				json.put("magnitude", sentiment.getMagnitude());
				json.put("score", sentiment.getScore());

				for (Sentence sentence : response.getSentencesList()) {
					objAux = new JSONObject();

					objAux.put("number", i++);

					if(sentence.getText().getBeginOffset() == -1) {
						objAux.put("offset", beginOffset);
						beginOffset += sentence.getText().getContent().length();
					} else {
						objAux.put("offset", sentence.getText().getBeginOffset());
					}
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

	@SuppressWarnings("unchecked")
	public boolean analyzeSentimentText() throws UnknownHostException, InterruptedException{
		LoadDocuments ld = new LoadDocuments(host, databaseName, collectionName);

		JSONArray jarr = ld.findByQuery(new BasicDBObject().append("is_sentiment", "false"),  3200);

		int length = jarr.size() / NUMBERTHREAD;

		if(length == 0)
			return true;
		
		Thread[] tr = new Thread[NUMBERTHREAD];
		for (int i = 0; i < NUMBERTHREAD; i++) {
			List<BasicDBObject> subList = jarr.subList(length * i, i + 1 < NUMBERTHREAD ? length * (i + 1) : jarr.size());
			
			JSONArray slJarr = new JSONArray();
			for(int du = 0; du < subList.size(); du++){
				slJarr.add((BasicDBObject) subList.get(du));
			}
			
			SentimentAnalysis sa = new SentimentAnalysis(host, databaseName, collectionName,
					slJarr);

			tr[i] = new Thread(sa);
			tr[i].start();
		}
		
		boolean isAlive = true;
		while(isAlive) {
			Thread.sleep(5000);
			System.out.println("Sentiment Analysis is alive!");
			
			isAlive = false;
			for(int i = 0; i < NUMBERTHREAD; i++)
				isAlive = isAlive || tr[i].isAlive();
		}

		return true;
	}
	
	@Override
	public void run() {
		Object id = null;
		try {
			SaveDocuments sd = new SaveDocuments(host, databaseName, collectionName);

			for (int i = 0; i < arr.size(); i++) {
				BasicDBObject json = (BasicDBObject) arr.get(i);
				
				id = json.get("_id");
				JSONObject sentiment = this.analyzeSentimentText(json.get("text").toString(),
						json.get("title").toString(), json.get("date").toString());
				
				JSONArray sentences = (JSONArray) sentiment.get("sentences");
				BasicDBList ltSentences = new BasicDBList();
				
				for(int j = 0; j < sentences.size(); j++){
					ltSentences.add(new BasicDBObject().append("number_sentence", ((JSONObject)sentences.get(j)).get("number"))
							.append("score", ((JSONObject)sentences.get(j)).get("score"))
							.append("magnitude", ((JSONObject)sentences.get(j)).get("magnitude"))
							.append("offset", ((JSONObject)sentences.get(j)).get("offset")));
				}
				
				sd.updateDocument(new BasicDBObject().append("$set", new BasicDBObject().append("is_sentiment", "true")
						.append("score_sentiment", sentiment.get("score"))
						.append("magnitude_sentiment", sentiment.get("magnitude"))
						.append("sentiments", ltSentences)),
						new BasicDBObject().append("_id", json.get("_id")));
			}
		} catch (Exception e) {
			System.out.println(id);
			e.printStackTrace();
		}
	}
}
