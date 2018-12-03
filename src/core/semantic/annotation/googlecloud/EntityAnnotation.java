package core.semantic.annotation.googlecloud;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import io.db.LoadDocuments;
import io.db.SaveDocuments;

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
				boolean input = false;

				JSONObject jsonEntity = new JSONObject();

				jsonEntity.put("type", entity.getType().toString());
				jsonEntity.put("name", entity.getName());
				jsonEntity.put("salience", entity.getSalience());

				JSONObject jsonMetadata = new JSONObject();
				for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
					jsonMetadata.put(entry.getKey(), entry.getValue());
				}
				jsonEntity.put("metadado", jsonMetadata);

				JSONArray jsonAux = new JSONArray();
				for (EntityMention mention : entity.getMentionsList()) {
					JSONObject jsonMention = new JSONObject();

					jsonMention.put("offset", mention.getText().getBeginOffset());
					jsonMention.put("content", mention.getText().getContent());
					jsonMention.put("type", mention.getType().toString());

					jsonAux.add(jsonMention);

					if (mention.getType().toString().equals("PROPER"))
						input = true;
				}
				jsonEntity.put("mentions", jsonAux);

				if (input)
					jsonArray.add(jsonEntity);

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

	@SuppressWarnings("unchecked")
	public boolean analyzeEntitiesText() throws UnknownHostException, ParseException, InterruptedException {
		LoadDocuments ld = new LoadDocuments(host, databaseName, collectionNameFind);

		JSONArray jarr = ld.findByQuery(new BasicDBObject().append("is_entityannotation", "false"), 47);

		int length = jarr.size() / NUMBERTHREAD;

		if (length == 0)
			return true;

		Thread[] tr = new Thread[NUMBERTHREAD];
		for (int i = 0; i < NUMBERTHREAD; i++) {
			List<BasicDBObject> subList = jarr.subList(length * i,
					i + 1 < NUMBERTHREAD ? length * (i + 1) : jarr.size());

			JSONArray slJarr = new JSONArray();
			for (int du = 0; du < subList.size(); du++) {
				slJarr.add((BasicDBObject) subList.get(du));
			}

			EntityAnnotation ea = new EntityAnnotation(host, databaseName, collectionNameSave, collectionNameFind,
					slJarr);

			tr[i] = new Thread(ea);
			tr[i].start();
		}
		
		boolean isAlive = true;
		while(isAlive) {
			Thread.sleep(5000);
			System.out.println("Entity Annotation is alive!");
			
			isAlive = false;
			for(int i = 0; i < NUMBERTHREAD; i++)
				isAlive = isAlive || tr[i].isAlive();
		}

		return true;
	}

	@SuppressWarnings({ "deprecation"})
	@Override
	public void run() {

		try {
			SaveDocuments sd = new SaveDocuments(host, databaseName, collectionNameSave);
			LoadDocuments ld = new LoadDocuments(host, databaseName, collectionNameSave);

			for (int i = 0; i < arr.size(); i++) {

				BasicDBObject bdbo = (BasicDBObject) arr.get(i);

				JSONObject entities = this.analyzeEntitiesText(bdbo.getString("text"), bdbo.get("title").toString(),
						bdbo.getString("date"));

				BasicDBList ltEntities = new BasicDBList();

				JSONArray mentions = (JSONArray) entities.get("entities");
				for (int j = 0; j < mentions.size(); j++) {
					JSONObject entity = (JSONObject) mentions.get(j);

					BasicDBObject query = new BasicDBObject();
					List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
					obj.add(new BasicDBObject("entity", entity.get("name").toString().toUpperCase()));
					obj.add(new BasicDBObject("type", entity.get("type")));
					query.put("$and", obj);

					JSONArray find = ld.findByQuery(query);

					// New Entity
					if (find.size() == 0) {
						ltEntities.add(new BasicDBObject().append("entity", entity.get("name").toString().toUpperCase())
								.append("type", entity.get("type")));

						BasicDBObject newEntity = new BasicDBObject();
						newEntity.append("entity", entity.get("name").toString().toUpperCase());
						newEntity.append("type", entity.get("type").toString());

						if (entity.containsKey("metadado")
								&& ((JSONObject) entity.get("metadado")).containsKey("wikipedia_url"))
							newEntity.append("url_source", ((JSONObject) entity.get("metadado")).get("wikipedia_url"));

						JSONArray docMentions = (JSONArray) entity.get("mentions");
						BasicDBList ltMentions = new BasicDBList();

						int numberDirectMentions = 0;
						int numberCorefMentions = 0;
						for (int k = 0; k < docMentions.size(); k++) {
							String typeMention = ((JSONObject) docMentions.get(k)).get("type").toString();

							if (typeMention.equals("PROPER"))
								numberDirectMentions++;
							else
								numberCorefMentions++;

							ltMentions.add(new BasicDBObject()
									.append("offset",
											Integer.parseInt(
													((JSONObject) docMentions.get(k)).get("offset").toString()))
									.append("content", ((JSONObject) docMentions.get(k)).get("content").toString())
									.append("type", typeMention));
						}

						BasicDBList listDocuments = new BasicDBList();
						listDocuments.add(new BasicDBObject().append("id_document", bdbo.get("_id"))
								.append("date", bdbo.get("date")).append("number_direct_mentions", numberDirectMentions)
								.append("number_coref_mentions", numberCorefMentions).append("mentions", ltMentions));
						newEntity.append("documents", listDocuments);

						sd.insertDocument(collectionNameSave, newEntity);

					} else {
						// Update Old Entity
						BasicDBObject oldEntity = (BasicDBObject) find.get(0);

						BasicDBList ltDocuments = (BasicDBList) oldEntity.get("documents");

						int indexDocument = -1;
						for (int k = 0; k < ltDocuments.size(); k++)
							if (((BasicDBObject) ltDocuments.get(k)).get("id_document").equals(bdbo.get("_id"))) {
								indexDocument = k;
								break;
							}

						if(indexDocument == -1)
							ltEntities.add(new BasicDBObject().append("entity", entity.get("name").toString().toUpperCase())
									.append("type", entity.get("type")));
						
						JSONArray docMentions = (JSONArray) entity.get("mentions");
						BasicDBList ltMentions = indexDocument == -1 ? new BasicDBList()
								: (BasicDBList) ((BasicDBObject) ltDocuments.get(indexDocument)).get("mentions");

						int numberDirectMentions = indexDocument == -1 ? 0
								: ((BasicDBObject) ltDocuments.get(indexDocument)).getInt("number_direct_mentions");
						int numberCorefMentions = indexDocument == -1 ? 0
								: ((BasicDBObject) ltDocuments.get(indexDocument)).getInt("number_coref_mentions");

						for (int k = 0; k < docMentions.size(); k++) {
							String typeMention = ((JSONObject) docMentions.get(k)).get("type").toString();
							int offset = Integer.parseInt(((JSONObject) docMentions.get(k)).get("offset").toString());
							boolean cont = false;
							
							for(int pn = 0; pn < ltMentions.size(); pn++)
								if(((BasicDBObject)ltMentions.get(pn)).getInt("offset") == offset) {
									cont = true;
									break;
								}
							
							if(cont)
								continue;

							if (typeMention.equals("PROPER"))
								numberDirectMentions++;
							else
								numberCorefMentions++;

							ltMentions.add(new BasicDBObject()
									.append("offset", offset)
									.append("content", ((JSONObject) docMentions.get(k)).get("content").toString())
									.append("type", typeMention));
						}

						if (indexDocument == -1) {
							ltDocuments.add(new BasicDBObject().append("id_document", bdbo.get("_id"))
									.append("date", bdbo.get("date"))
									.append("number_direct_mentions", numberDirectMentions)
									.append("number_coref_mentions", numberCorefMentions)
									.append("mentions", ltMentions));
						} else {
							((BasicDBObject) ltDocuments.get(indexDocument)).replace("number_direct_mentions", numberDirectMentions);
							((BasicDBObject) ltDocuments.get(indexDocument)).replace("number_coref_mentions", numberCorefMentions);
							((BasicDBObject) ltDocuments.get(indexDocument)).replace("mentions", ltMentions);

						}

						if ((!oldEntity.containsKey("url_source") || oldEntity.get("url_source").equals(""))
								&& (entity.containsKey("url_source") && !entity.get("url_source").equals("")))
							sd.updateDocument(collectionNameSave,
									new BasicDBObject().append("$set",
											new BasicDBObject().append("url_source", entity.get("url_source"))),
									new BasicDBObject().append("_id", oldEntity.get("_id").toString()));

						sd.updateDocument(collectionNameSave,
								new BasicDBObject().append("$set",
										new BasicDBObject().append("documents", ltDocuments)),
								new BasicDBObject().append("_id", oldEntity.get("_id")));
					}
				}

				sd.updateDocument(collectionNameFind,
						new BasicDBObject().append("$set",
								new BasicDBObject().append("is_entityannotation", "true").append("entities",
										ltEntities)),
						new BasicDBObject().append("_id", bdbo.get("_id")));

				/*
				 * sd.updateDocument(collectionNameFind, new BasicDBObject().append("$set", new
				 * BasicDBObject().append("entities", ltEntities)), new
				 * BasicDBObject().append("_id", ((JSONObject) arr.get(i)).get("_id")));
				 */
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
