package core.semantic.annotation.googlecloud;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import io.db.LoadDocuments;
import io.db.SaveDocuments;

import com.google.cloud.language.v1.Document.Type;

public class EntityAnnotation implements Runnable {

	private final int NUMBERTHREAD = 1;
	private JSONArray arr;
	private String host;
	private String databaseName;
	private String collectionNameSave;
	private String collectionNameFind;

	public EntityAnnotation(String host, String databaseName, String collectionNameSave, String collectionNameFind) {
		arr = null;
		this.host = host;
		this.databaseName = databaseName;
		this.collectionNameSave = collectionNameSave;
		this.collectionNameFind = collectionNameFind;
	}

	public EntityAnnotation(String host, String databaseName, String collectionNameSave, String collectionNameFind,
			JSONArray arr) {
		this.arr = arr;
		this.host = host;
		this.databaseName = databaseName;
		this.collectionNameSave = collectionNameSave;
		this.collectionNameFind = collectionNameFind;
	}

	@SuppressWarnings("unchecked")
	public JSONObject analyzeEntitiesText(String text, String title, String date) throws Exception {
		JSONObject json = new JSONObject();
		JSONArray jsonArray = new JSONArray();

		try (LanguageServiceClient language = LanguageServiceClient.create()) {
			Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
			AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder().setDocument(doc)
					.setEncodingType(EncodingType.UTF8).build();

			AnalyzeEntitiesResponse response = language.analyzeEntities(request);

			json.put("date", date);
			json.put("tittle", title);

			// Print the response
			for (Entity entity : response.getEntitiesList()) {

				if (entity.getType().getNumber() >= 1 && entity.getType().getNumber() <= 3) {
					JSONObject jsonEntity = new JSONObject();

					jsonEntity.put("type", entity.getType().toString());
					jsonEntity.put("name", entity.getName());
					jsonEntity.put("salience", entity.getSalience());

					JSONArray jsonAux = new JSONArray();
					for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
						JSONObject jsonMetadata = new JSONObject();
						jsonMetadata.put(entry.getKey(), entry.getValue());
						jsonAux.add(jsonMetadata);
					}
					jsonEntity.put("metadado", jsonAux);

					jsonAux = new JSONArray();
					for (EntityMention mention : entity.getMentionsList()) {
						JSONObject jsonMention = new JSONObject();

						jsonMention.put("offset", mention.getText().getBeginOffset());
						jsonMention.put("content", mention.getText().getContent());
						jsonMention.put("type", mention.getType().toString());

						jsonAux.add(jsonMention);
					}
					jsonEntity.put("mentions", jsonAux);

					jsonArray.add(jsonEntity);
				}
			}
		}

		json.put("entities", jsonArray);
		return json;
	}

	public static void analyzeEntitiesFile(String gcsUri) throws Exception {

		try (LanguageServiceClient language = LanguageServiceClient.create()) {
			Document doc = Document.newBuilder().setGcsContentUri(gcsUri).setType(Type.PLAIN_TEXT).build();
			AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder().setDocument(doc)
					.setEncodingType(EncodingType.UTF16).build();

			AnalyzeEntitiesResponse response = language.analyzeEntities(request);
			for (Entity entity : response.getEntitiesList()) {
				System.out.printf("Entity: %s", entity.getName());
				System.out.printf("Salience: %.3f\n", entity.getSalience());
				System.out.println("Metadata: ");
				for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
					System.out.printf("%s : %s", entry.getKey(), entry.getValue());
				}
				for (EntityMention mention : entity.getMentionsList()) {
					System.out.printf("Begin offset: %d\n", mention.getText().getBeginOffset());
					System.out.printf("Content: %s\n", mention.getText().getContent());
					System.out.printf("Type: %s\n\n", mention.getType());
				}
			}
		}
	}

	public boolean analyzeEntitiesText() throws UnknownHostException {
		LoadDocuments ld = new LoadDocuments(host, databaseName, collectionNameFind);

		JSONArray jarr = ld.findByQuery(new BasicDBObject().append("is_entityannotation", "false"),  1);

		int length = jarr.size() / NUMBERTHREAD;

		for (int i = 0; i < NUMBERTHREAD; i++) {
			EntityAnnotation ea = new EntityAnnotation(host, databaseName, collectionNameSave, collectionNameFind,
					(JSONArray) jarr.subList(length * i, i + 1 < NUMBERTHREAD ? length * (i + 1) : jarr.size()));

			(new Thread(ea)).start();
		}

		return true;
	}

	@Override
	public void run() {

		try {
			SaveDocuments sd = new SaveDocuments(host, databaseName, collectionNameSave);
			LoadDocuments ld = new LoadDocuments(host, databaseName, collectionNameSave);

			for (int i = 0; i < arr.size(); i++) {

				JSONObject json = (JSONObject) arr.get(i);

				JSONObject entities = this.analyzeEntitiesText(json.get("text").toString(),
						json.get("title").toString(), json.get("date_published").toString());

				BasicDBList ltEntities = new BasicDBList();
				
				JSONArray mentions = (JSONArray) entities.get("entities");
				for (int j = 0; i < mentions.size(); i++) {
					JSONObject entity = (JSONObject) mentions.get(j);
					
					ltEntities.add(new BasicDBObject().append("entity", entity.get("entity")).append("type", entity.get("type")));
					
					BasicDBObject query = new BasicDBObject();
					List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
					obj.add(new BasicDBObject("entity", entity.get("entity")));
					obj.add(new BasicDBObject("type", entity.get("type")));
					query.put("$and", obj);
					
					JSONArray find = ld.findByQuery(query);
					
					if (find.size() == 0) {
						
						BasicDBObject newEntity = new BasicDBObject();
						newEntity.append("entity", entity.get("entity").toString());
						newEntity.append("type", entity.get("type").toString());
						
						if(entity.containsKey("metadado") && ((JSONObject) entity.get("metadado")).containsKey("wikipedia_url"))
							newEntity.append("url_source", ((JSONObject) entity.get("metadado")).get("wikipedia_url"));
						
						JSONArray docMentions = (JSONArray) entity.get("mentions");
						BasicDBList ltMentions = new BasicDBList();
						
						int numberDirectMentions = 0;
						int numberCorefMentions = 0;
						for(int k = 0; k < docMentions.size(); k++) {
							String typeMention = ((JSONObject) docMentions.get(i)).get("type").toString();
							
							if(typeMention.equals("PROPER"))
								numberDirectMentions++;
							else
								numberCorefMentions++;
							
							ltMentions.add(new BasicDBObject().append("offset", Double.parseDouble(((JSONObject) docMentions.get(i)).get("offset").toString()))
									.append("content", ((JSONObject) docMentions.get(i)).get("content").toString())
									.append("type", typeMention));
						}
						
						newEntity.append("documents", new BasicDBList().add(new BasicDBObject().append("id_document", ((JSONObject) arr.get(i)).get("_id"))
								.append("date_published", ((JSONObject) arr.get(i)).get("date_published"))
								.append("number_direct_mentions", numberDirectMentions)
								.append("number_coref_mentions", numberCorefMentions)
								.append("mentions", ltMentions)));
						
						sd.insertDocument(collectionNameSave, newEntity);
					
					} else {
						JSONObject oldEntity = (JSONObject) find.get(i);

						//POSSÍVEL ERRO
						BasicDBList ltDocuments = (BasicDBList) oldEntity.get("documents");
						
						JSONArray docMentions = (JSONArray) entity.get("mentions");
						BasicDBList ltMentions = new BasicDBList();
						
						int numberDirectMentions = 0;
						int numberCorefMentions = 0;
						for(int k = 0; k < docMentions.size(); k++) {
							String typeMention = ((JSONObject) docMentions.get(i)).get("type").toString();
							
							if(typeMention.equals("PROPER"))
								numberDirectMentions++;
							else
								numberCorefMentions++;
							
							ltMentions.add(new BasicDBObject().append("offset", Double.parseDouble(((JSONObject) docMentions.get(i)).get("offset").toString()))
									.append("content", ((JSONObject) docMentions.get(i)).get("content").toString())
									.append("type", typeMention));
						}
						
						ltDocuments.add(new BasicDBObject().append("id_document", ((JSONObject) arr.get(i)).get("_id"))
								.append("date_published", ((JSONObject) arr.get(i)).get("date_published"))
								.append("number_direct_mentions", numberDirectMentions)
								.append("number_coref_mentions", numberCorefMentions)
								.append("mentions", ltMentions));
						
						if((!oldEntity.containsKey("url_source") || oldEntity.get("url_source").equals("")) && (entity.containsKey("url_source") && !entity.get("url_source").equals("")))
							sd.updateDocument(collectionNameSave, 
									new BasicDBObject().append("$set", new BasicDBObject().append("url_source", entity.get("url_source"))), 
									new BasicDBObject().append("_id", oldEntity.get("_id").toString()));
						
						sd.updateDocument(collectionNameSave, 
								new BasicDBObject().append("$set", new BasicDBObject().append("documents", ltDocuments)), 
								new BasicDBObject().append("_id", oldEntity.get("_id").toString()));
					}
				}

				sd.updateDocument(collectionNameFind,
						new BasicDBObject().append("$set", new BasicDBObject().append("is_entityannotation", "true")),
						new BasicDBObject().append("_id", ((JSONObject) arr.get(i)).get("_id").toString()));
				
				sd.updateDocument(collectionNameFind,
						new BasicDBObject().append("$set", new BasicDBObject().append("entities", ltEntities)),
						new BasicDBObject().append("_id", ((JSONObject) arr.get(i)).get("_id").toString()));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
