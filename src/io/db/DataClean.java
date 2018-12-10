package io.db;

import java.net.UnknownHostException;

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

	// Pegar o nome, o tipo e o url_source
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
							throw new Exception("Existe Documento. Código a Implementar.");
							//o documento exite no documentBase tenho que verificar as menções
							//exists = true;
						}
					
					//O documento não existe em documentsBase
					if(!exists) {
						documentsBase.add(documents.get(k));
					}
				}
				
				//excluo a mentions
				sd.removeDocument(new BasicDBObject().append("_id", ((BasicDBObject)jarr.get(j)).get("_id")));
			}
			
			if(indexBase != -1) {
				//Salvo o novo documento
				sd.updateDocument(
						new BasicDBObject().append("$set", new BasicDBObject().append("documents", documentsBase)), 
						new BasicDBObject().append("_id", ((BasicDBObject)jarr.get(indexBase)).get("_id")));
			}
		}
	}
}
