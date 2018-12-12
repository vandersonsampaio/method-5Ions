package io.db;

import org.json.simple.JSONArray;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class DataClean {
	private String host;
	private String databaseName;
	private String collection;
	private String collectionExternalFile;

	public static void main(String[] args) {
		DataClean dc = new DataClean("localhost", "db_news_brazil", "mentions", "externalfile");
		try {
			dc.joinDocuments("real");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DataClean(String host, String databaseName, String collection, String collectionExternalFile) {
		this.host = host;
		this.databaseName = databaseName;
		this.collection = collection;
		this.collectionExternalFile = collectionExternalFile;
	}

	public void joinDocuments(String nameTarget) throws Exception {
		LoadDocuments ld = new LoadDocuments(host, databaseName, collectionExternalFile);
		SaveDocuments sd = new SaveDocuments(host, databaseName, collection);

		BasicDBObject externalTarget = ld
				.findOne(new BasicDBObject().append("name", nameTarget).append("is_target", true));
		BasicDBObject entityTarget = (BasicDBObject) externalTarget.get("values");

		BasicDBList entitiesRelation = (BasicDBList) entityTarget.get("relations");
		entitiesRelation.add(entityTarget);

		// Obter as menções por url_source
		for (int i = 0; i < entitiesRelation.size(); i++) {
			BasicDBObject current = ((BasicDBObject) entitiesRelation.get(i));
			
			if(current.get("url_source") == null)
				continue;
			
			JSONArray jarr = ld.findByQuery(collection, new BasicDBObject().append("url_source",
					current.getString("url_source")));

			if(jarr.size() <= 1)
				continue;
			
			int indexBase = -1;
			for (int j = 0; j < jarr.size(); j++) {
				if(((BasicDBObject)jarr.get(j)).getString("entity").equals(current.getString("name")) && 
						((BasicDBObject)jarr.get(j)).getString("type").equals(current.getString("type"))){
					indexBase = j;
					break;
				}
			}
			
			BasicDBList documentsBase = (BasicDBList) ((BasicDBObject)jarr.get(indexBase)).get("documents");
			
			for (int j = 0; j < jarr.size(); j++) {
				if(indexBase == j)
					continue;
				
				BasicDBList documents = (BasicDBList) ((BasicDBObject)jarr.get(j)).get("documents");
				
				for(int k = 0; k < documents.size(); k++) {
					boolean exists = false;
					for(int du = 0; du < documentsBase.size(); du++)
						if(((BasicDBObject) documents.get(k)).get("id_document").equals(((BasicDBObject) documentsBase.get(du)).get("id_document"))) {
							//o documento exite no documentBase tenho que verificar as menções
							BasicDBList mentionsBase = (BasicDBList) ((BasicDBObject) documentsBase.get(du)).get("mentions");
							BasicDBList mentions = (BasicDBList) ((BasicDBObject) documents.get(k)).get("mentions");
							for(int ca = 0; ca < mentions.size(); ca++) {
								boolean existsMention = false;
								for(int van = 0; van < mentionsBase.size(); van++) {
									if(((BasicDBObject) mentions.get(ca)).getInt("offset") == ((BasicDBObject) mentionsBase.get(van)).getInt("offset")) {
										existsMention = true;
									}
								}
								
								if(!existsMention) {
									//atualizar a contagem
									if(((BasicDBObject) mentions.get(ca)).getString("type").equals("PROPER"))
										((BasicDBObject) documentsBase.get(du)).replace("number_direct_mentions", 
												((BasicDBObject) documentsBase.get(du)).getInt("number_direct_mentions") + 1);
									else 
										((BasicDBObject) documentsBase.get(du)).replace("number_coref_mentions", 
												((BasicDBObject) documentsBase.get(du)).getInt("number_coref_mentions") + 1);
									
									((BasicDBList) ((BasicDBObject) documentsBase.get(du)).get("mentions")).add((BasicDBObject) mentions.get(ca));
								}
							}
							
							exists = true;
							break;
						}
					
					//O documento não existe em documentsBase
					if(!exists) {
						documentsBase.add(documents.get(k));
					}
					
					//TESTAR
					//Altera o apontamento do documento para a entidade. Verificando se já existe apontamento.
					BasicDBObject docAlter = ld.findOne("documents", new BasicDBObject().append("_id", ((BasicDBObject) documents.get(k)).get("id_document")));
					BasicDBList listAlter = (BasicDBList) docAlter.get("entities");
					boolean existsAlter = true;
					int indexRemove = -1;
					for(int van = 0; van < listAlter.size(); van++){
						BasicDBObject m = (BasicDBObject) listAlter.get(van);
						if(m.getString("entity").equals(((BasicDBObject)jarr.get(j)).getString("entity") ) && m.getString("type").equals(((BasicDBObject)jarr.get(j)).getString("type") ))
							indexRemove = van;
						
						if(m.getString("entity").equals(((BasicDBObject)jarr.get(indexBase)).getString("entity") ) && m.getString("type").equals(((BasicDBObject)jarr.get(indexBase)).getString("type") ))
							existsAlter = false;
					}
					
					if(indexRemove != -1)
						listAlter.remove(indexRemove);
					
					if(existsAlter)
						listAlter.add(new BasicDBObject().append("entity", ((BasicDBObject)jarr.get(indexBase)).getString("entity"))
								.append("type", ((BasicDBObject)jarr.get(indexBase)).getString("type")));
					
					if(indexRemove != -1 || existsAlter)
						sd.updateDocument("documents",
								new BasicDBObject().append("$set", new BasicDBObject().append("entities", listAlter)), 
								new BasicDBObject().append("_id", docAlter.get("_id")));
				}
				
				//excluo a mentions
				sd.removeDocument("mentions",
						new BasicDBObject().append("_id", ((BasicDBObject)jarr.get(j)).get("_id")));
			}
			
			if(indexBase != -1) {
				//Salvo o novo documento
				sd.updateDocument("mentions",
						new BasicDBObject().append("$set", new BasicDBObject().append("documents", documentsBase)), 
						new BasicDBObject().append("_id", ((BasicDBObject)jarr.get(indexBase)).get("_id")));
			}
		}
	}
}
