package core.semantic.annotation.googlecloud;

import java.net.UnknownHostException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.cloud.language.v1.AnalyzeEntitySentimentRequest;
import com.google.cloud.language.v1.AnalyzeEntitySentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.mongodb.BasicDBObject;

import io.db.LoadDocuments;
import io.db.SaveDocuments;

import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.LanguageServiceClient;

public class SentimentEntityAnnotation implements Runnable {

	private final int NUMBERTHREAD = 1;
	private JSONArray arr;
	private String host;
	private String databaseName;
	private String collectionNameSave;
	private String collectionNameFind;
	
	public SentimentEntityAnnotation(String host, String databaseName, String collectionNameSave, String collectionNameFind) {
		arr = null;
		this.host = host;
		this.databaseName = databaseName;
		this.collectionNameSave = collectionNameSave;
		this.collectionNameFind = collectionNameFind;
	}

	public SentimentEntityAnnotation(String host, String databaseName, String collectionNameSave, String collectionNameFind,
			JSONArray arr) {
		this.arr = arr;
		this.host = host;
		this.databaseName = databaseName;
		this.collectionNameSave = collectionNameSave;
		this.collectionNameFind = collectionNameFind;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject entitySentimentText(String text, String tittle, String date) throws Exception {
		// Salvar sentimento por entidade, preferencialmente por sentença
		JSONObject json = new JSONObject();

		try (LanguageServiceClient language = LanguageServiceClient.create()) {
			Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
			AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest.newBuilder().setDocument(doc)
					.setEncodingType(EncodingType.UTF16).build();
			// detect entity sentiments in the given string
			AnalyzeEntitySentimentResponse response = language.analyzeEntitySentiment(request);
			// Print the response

			json.put("date", date);
			json.put("lengthText", text.split(" ").length);
			
			//json.put("tittle", tittle);
			
			JSONArray arrEntities = new JSONArray();
			JSONObject objEntity = null;

			for (Entity entity : response.getEntitiesList()) {
				if (entity.getType().getNumber() >= 1 && entity.getType().getNumber() <= 3) {
					objEntity = new JSONObject();
					
					objEntity.put("type", entity.getType().toString());
					if(entity.getMetadataCount() == 0) {
						objEntity.put("name", entity.getName());
					} else {
						if(entity.getMetadataMap().get("wikipedia_url") != null){
							String[] parts = entity.getMetadataMap().get("wikipedia_url").toString().split("/");
							objEntity.put("name", parts[parts.length-1]);
						}else{
							objEntity.put("name", entity.getName());
						}
					}
					
					objEntity.put("salience", entity.getSalience());
					
					JSONObject objSentiment = new JSONObject();
					objSentiment.put("magnitude", entity.getSentiment().getMagnitude());
					objSentiment.put("score", entity.getSentiment().getScore());
					objEntity.put("sentiment", objSentiment);
					
					if(entity.getSentiment().getScore() == 0)
						continue;
					
					JSONArray arrMention = new JSONArray();
					JSONObject objMention = null;
					
					for (EntityMention mention : entity.getMentionsList()) {
						objMention = new JSONObject();
						
						objMention.put("offset", mention.getText().getBeginOffset());
						objMention.put("content", mention.getText().getContent());
						objMention.put("magnitude", mention.getSentiment().getMagnitude());
						objMention.put("score", mention.getSentiment().getScore());
						objMention.put("type", mention.getType().toString());
						
						arrMention.add(objMention);
					}

					objEntity.put("mentions", arrMention);
					arrEntities.add(objEntity);
				}
				
				json.put("entities", arrEntities);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return json;
	}

	/**
	 * Identifies the entity sentiments in the the GCS hosted file using the
	 * Language Beta API.
	 */
	public static void entitySentimentFile(String gcsUri) throws Exception {
		// [START entity_sentiment_file]
		// Instantiate the Language client
		// com.google.cloud.language.v1.LanguageServiceClient
		try (LanguageServiceClient language = LanguageServiceClient.create()) {
			Document doc = Document.newBuilder().setGcsContentUri(gcsUri).setType(Type.PLAIN_TEXT).build();
			AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest.newBuilder().setDocument(doc)
					.setEncodingType(EncodingType.UTF16).build();
			// Detect entity sentiments in the given file
			AnalyzeEntitySentimentResponse response = language.analyzeEntitySentiment(request);
			// Print the response
			for (Entity entity : response.getEntitiesList()) {
				System.out.printf("Entity: %s\n", entity.getName());
				System.out.printf("Salience: %.3f\n", entity.getSalience());
				System.out.printf("Sentiment : %s\n", entity.getSentiment());
				for (EntityMention mention : entity.getMentionsList()) {
					System.out.printf("Begin offset: %d\n", mention.getText().getBeginOffset());
					System.out.printf("Content: %s\n", mention.getText().getContent());
					System.out.printf("Magnitude: %.3f\n", mention.getSentiment().getMagnitude());
					System.out.printf("Sentiment score : %.3f\n", mention.getSentiment().getScore());
					System.out.printf("Type: %s\n\n", mention.getType());
				}
			}
		}
	}

	public boolean entitySentimentText() throws UnknownHostException{
		LoadDocuments ld = new LoadDocuments(host, databaseName, collectionNameFind);

		JSONArray jarr = ld.findByQuery(new BasicDBObject().append("is_entitysentiment", "false"),  1);

		int length = jarr.size() / NUMBERTHREAD;

		for (int i = 0; i < NUMBERTHREAD; i++) {
			SentimentEntityAnnotation sea = new SentimentEntityAnnotation(host, databaseName, collectionNameSave, collectionNameFind,
					(JSONArray) jarr.subList(length * i, i + 1 < NUMBERTHREAD ? length * (i + 1) : jarr.size()));

			(new Thread(sea)).start();
		}

		return true;
	}
	
	@Override
	public void run() {
		//Colocar idioma (language) nos documentos
		//Se o idioma for diferente de Inglês devo agregar os sentimentos das sentenças que contem a entidade
		//Se o idioma for inglês devo aplicar o webservice da google
		try {
			SaveDocuments sd = new SaveDocuments(host, databaseName, collectionNameSave);
			LoadDocuments ld = new LoadDocuments(host, databaseName, collectionNameSave);
			
			for (int i = 0; i < arr.size(); i++) {
				//Pego as entidades contidas nesse documento (entities)
				//Pego os sentimentos das sentenças 
				
				//o offset da entidade estará entre dois offsets das sentenças
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
