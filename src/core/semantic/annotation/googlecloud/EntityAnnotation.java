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
import util.adapter.ImportValorEconomico;

import com.google.cloud.language.v1.Document.Type;

public class EntityAnnotation implements Runnable {

	private final int NUMBERTHREAD = 4;
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

		JSONArray jarr = ld.findByQuery(new BasicDBObject().append("is_entityannotation", "false"));

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

			for (int i = 0; i < arr.size(); i++) {

				JSONObject json = (JSONObject) arr.get(i);

				JSONObject entities = this.analyzeEntitiesText(json.get("text").toString(),
						json.get("title").toString(), json.get("date").toString());

				JSONArray mentions = (JSONArray) entities.get("entities");
				for (int j = 0; i < mentions.size(); i++) {
					JSONObject entity = (JSONObject) mentions.get(j);
					if (!sd.containsDocument("entity", entity.get("name").toString())) {
						//Nova entidade
						BasicDBObject newEntity = new BasicDBObject();
						newEntity.append("entity", entity.get("entity").toString());
						newEntity.append("type", entity.get("type").toString());
						
						JSONArray docMentions = (JSONArray) entity.get("mentions");
						BasicDBList ltMentions = new BasicDBList();
						
						for(int k = 0; k < docMentions.size(); k++) {
							//contar os direct mentions e as coref mentions
							ltMentions.add(new BasicDBObject().append("offset", Double.parseDouble(((JSONObject) docMentions.get(i)).get("offset").toString()))
									.append("content", ((JSONObject) docMentions.get(i)).get("content").toString())
									.append("type", ((JSONObject) docMentions.get(i)).get("offset").toString()));
						}
						
						
						
						sd.insertDocument((DBObject) JSON.parse(((JSONObject) mentions.get(j)).toJSONString()));
					
					} else {
					// Ajustar isso aqui para atualizar lista de documentos
					// BasicDBObject alter = new BasicDBObject().append("$set",
					// new BasicDBObject().append("entity",
					// mentions.get(j).get("entity").toString()));

					// BasicDBObject search = new BasicDBObject().append("entity",
					// mentions.get(i).get("entity").toString());

					// sd.updateDocument(alter, search);
					}
				}

				//Atualizar também a relação de entidades que aparece no documento
				sd.updateDocument(collectionNameFind,
						new BasicDBObject().append("$set", new BasicDBObject().append("is_entityannotation", "true")),
						new BasicDBObject().append("_id", ((JSONObject) arr.get(i)).get("_id").toString()));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
