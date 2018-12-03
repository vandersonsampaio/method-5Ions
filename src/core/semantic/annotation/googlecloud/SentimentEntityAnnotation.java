package core.semantic.annotation.googlecloud;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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

	public SentimentEntityAnnotation(String host, String databaseName, String collectionNameSave,
			String collectionNameFind) {
		arr = null;
		this.host = host;
		this.databaseName = databaseName;
		this.collectionNameSave = collectionNameSave;
		this.collectionNameFind = collectionNameFind;
	}

	public SentimentEntityAnnotation(String host, String databaseName, String collectionNameSave,
			String collectionNameFind, JSONArray arr) {
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

			JSONArray arrEntities = new JSONArray();
			JSONObject objEntity = null;

			for (Entity entity : response.getEntitiesList()) {
				if (entity.getType().getNumber() >= 1 && entity.getType().getNumber() <= 3) {
					objEntity = new JSONObject();

					objEntity.put("type", entity.getType().toString());
					objEntity.put("name", entity.getName());
					/*
					 * if(entity.getMetadataCount() == 0) { objEntity.put("name", entity.getName());
					 * } else { if(entity.getMetadataMap().get("wikipedia_url") != null){ String[]
					 * parts = entity.getMetadataMap().get("wikipedia_url").toString().split("/");
					 * objEntity.put("name", parts[parts.length-1]); }else{ objEntity.put("name",
					 * entity.getName()); } }
					 */

					objEntity.put("salience", entity.getSalience());

					JSONObject objSentiment = new JSONObject();
					objSentiment.put("magnitude", entity.getSentiment().getMagnitude());
					objSentiment.put("score", entity.getSentiment().getScore());
					objEntity.put("sentiment", objSentiment);

					if (entity.getSentiment().getScore() == 0)
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

	@SuppressWarnings("unchecked")
	public boolean entitySentimentText() throws UnknownHostException {
		LoadDocuments ld = new LoadDocuments(host, databaseName, collectionNameFind);
		
		JSONArray jarr = ld.findByQuery(new BasicDBObject().append("is_entitysentiment", "false"), 1);

		int length = jarr.size() / NUMBERTHREAD;

		if (length == 0)
			return true;

		for (int i = 0; i < NUMBERTHREAD; i++) {
			List<BasicDBObject> subList = jarr.subList(length * i, i + 1 < NUMBERTHREAD ? length * (i + 1) : jarr.size());

			JSONArray slJarr = new JSONArray();
			for (int du = 0; du < subList.size(); du++) {
				slJarr.add((BasicDBObject) subList.get(du));
			}

			SentimentEntityAnnotation sea = new SentimentEntityAnnotation(host, databaseName, collectionNameSave,
					collectionNameFind, slJarr);

			Thread t = new Thread(sea);
			// Temporário
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			SaveDocuments sd = new SaveDocuments(host, databaseName, collectionNameFind);
			LoadDocuments ld = new LoadDocuments(host, databaseName, collectionNameFind);

			for (int i = 0; i < arr.size(); i++) {
				BasicDBObject doc = (BasicDBObject) arr.get(i);
				if (doc.get("language").toString().equals("en")) {
					// Pego as entidades contidas nesse documento (entities)
					JSONObject mentions = this.entitySentimentText(doc.get("content").toString(),
							doc.get("title").toString(), doc.get("date").toString());
					JSONArray mentionsArr = (JSONArray) mentions.get("entities");

					// colocar mentions em um for para a cada menção buscar sua correspondente
					for (Object mention : mentionsArr) {
						double score_direct_sentiment_pos = 0;
						double score_direct_sentiment_neg = 0;
						double score_coref_sentiment_pos = 0;
						double score_coref_sentiment_neg = 0;

						// buscar em ld a entity correspondente
						JSONArray mentionsCollection = ld.findByQuery(
								new BasicDBObject().append("entity", ((JSONObject) mention).get("name").toString())
										.append("type", ((JSONObject) mention).get("type").toString()));

						// pegar em mentionscollection o document correspondente
						JSONArray documents = (JSONArray) ((JSONObject) mentionsCollection.get(0)).get("documents");
						int indexDocument = -1;

						for (int k = 0; k < documents.size(); k++) {
							if (((JSONObject) documents.get(k)).get("id_document").equals(doc.get("id"))) {
								indexDocument = k;
								break;
							}
						}

						JSONArray mentionArr = (JSONArray) ((JSONObject) mention).get("mentions");

						// contabilizar o score_direct_sentiment e o score_coref_sentiment
						for (int k = 0; k < mentionArr.size(); k++) {
							double value = (double) ((JSONObject) mentionArr.get(k)).get("score");
							if (((JSONObject) mentionArr.get(k)).get("type").toString().equals("PROPER"))
								if (value >= 0)
									score_direct_sentiment_pos += value;
								else
									score_direct_sentiment_neg += (value * -1);
							else if (value >= 0)
								score_coref_sentiment_pos += value;
							else
								score_coref_sentiment_neg += (value * -1);
						}

						((JSONObject) documents.get(indexDocument)).put("sentiments",
								new BasicDBObject().append("score_direct_pos", score_direct_sentiment_pos)
										.append("score_direct_neg", score_direct_sentiment_neg)
										.append("score_coref_pos", score_coref_sentiment_pos)
										.append("score_coref_neg", score_coref_sentiment_neg));

						// Atualizar o documents do mentionsCollection adicionando o atributo sentiment
						// com dois atributos (score_direct e score_coref)
						// CONFIRMAR ISSO
						sd.updateDocument(
								new BasicDBObject().append("$set", new BasicDBObject().append("documents", documents)),
								new BasicDBObject().append("entity", ((JSONObject) mention).get("entity").toString())
										.append("type", ((JSONObject) mention).get("type").toString()));
					}

				} else {
					JSONArray entities = (JSONArray) doc.get("entities");

					// buscar em doc as sentenças e seus sentimentos
					JSONArray sentiments = (JSONArray) doc.get("sentiments");

					for (int j = 0; j < entities.size(); j++) {
						double score_direct_sentiment_pos = 0;
						double score_direct_sentiment_neg = 0;
						double score_coref_sentiment_pos = 0;
						double score_coref_sentiment_neg = 0;

						// Consultar a entidade em mentions (entities possui atributos entity e type)
						JSONObject entity = (JSONObject) entities.get(i);
						JSONArray mentionsCollection = ld
								.findByQuery(new BasicDBObject().append("entity", entity.get("entity").toString())
										.append("type", entity.get("type").toString()));

						if (mentionsCollection.size() > 0) {
							// pegar o atributo documents do retorno da linha anterior
							JSONArray documents = (JSONArray) ((JSONObject) mentionsCollection.get(0)).get("documents");
							int indexDocument = -1;

							// pesquisar o documento que possui o mesmo id_documento da variável doc
							for (int k = 0; k < documents.size(); k++) {
								if (((JSONObject) documents.get(k)).get("id_document").equals(doc.get("id"))) {
									indexDocument = k;
									break;
								}
							}

							// pegar o atributo metions da linha anteior
							for (Object mention : (JSONArray) ((JSONObject) documents.get(indexDocument))
									.get("mentions")) {
								int offset = (int) ((JSONObject) mention).get("offset");
								String type = ((JSONObject) mention).get("type").toString();
								double score = 0;
								// identificar qual sentença a mentions está
								// se a mentions for proper atualizar o atributo score_direct_sentiment, senão
								// atualizar score_coref_sentiment
								// atualizar a collection mentions
								for (int k = 0; k < sentiments.size(); k++) {
									// Pego os sentimentos das sentenças
									// o offset da entidade estará entre dois offsets das sentenças
									if ((k + 1 == sentiments.size()) || (((int) ((JSONObject) sentiments.get(k))
											.get("offset")) <= offset
											&& ((int) ((JSONObject) sentiments.get(k + 1)).get("offset")) >= offset))
										score = (double) ((JSONObject) sentiments.get(k)).get("score");

								}

								if (type.equals("PROPER"))
									if (score >= 0)
										score_direct_sentiment_pos += score;
									else
										score_direct_sentiment_neg += (score * -1);
								else if (score >= 0)
									score_coref_sentiment_pos += score;
								else
									score_coref_sentiment_neg += (score * -1);
							}

							((JSONObject) documents.get(indexDocument)).put("sentiments",
									new BasicDBObject().append("score_direct_pos", score_direct_sentiment_pos)
											.append("score_direct_neg", score_direct_sentiment_neg)
											.append("score_coref_pos", score_coref_sentiment_pos)
											.append("score_coref_neg", score_coref_sentiment_neg));

							// Atualizar o documents do mentionsCollection adicionando o atributo sentiment
							// com dois atributos (score_direct e score_coref)
							// CONFIRMAR ISSO
							sd.updateDocument(
									new BasicDBObject().append("$set",
											new BasicDBObject().append("documents", documents)),
									new BasicDBObject().append("entity", entity.get("entity").toString()).append("type",
											entity.get("type").toString()));
						}
					}
				}

				// Atualizar o document
				// CONFIRMAR ISSO
				sd.updateDocument(collectionNameFind,
						new BasicDBObject().append("$set", new BasicDBObject().append("is_entitysentiment", "true")),
						new BasicDBObject().append("_id", doc.get("_id")));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
