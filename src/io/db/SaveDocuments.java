package io.db;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class SaveDocuments {
	
	private MongoDB mongo;
	
	public static void main(String[] args){
		try {
			SaveDocuments sd = new SaveDocuments("localhost", "db_poll_fakenews", "documents");
			
			//DBObject doc = (DBObject) JSON.parse("{'title': 'Titulo da materia', 'text':'O corpo da materia sem acentuacao.', 'date': '2018-11-12', 'url': 'www.test.com', 'source': 'Teste', 'type': 'test'}");
			
			//sd.insertDocument("documents", doc);
			
			System.out.println(sd.containsDocument("title", "Titulo da materia"));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SaveDocuments(String url, String database, String collection) throws UnknownHostException{
		this.mongo = new MongoDB(url);
		this.mongo.setDataBase(database);
		this.mongo.setCollection(collection);
	}
	
	public void insertDocument(String collection, DBObject doc){
		this.mongo.setCollection(collection);
		
		this.insertDocument(doc);
	}
	
	public void insertDocument(String collection, JSON doc){
		this.mongo.setCollection(collection);
		
		this.insertDocument((DBObject) doc);
	}
	
	public void insertDocument(DBObject doc){
		this.mongo.insertDocument(doc);
	}
	
	public boolean containsDocument(String collection, String key, String value){
		this.mongo.setCollection(collection);
		
		return this.containsDocument(key, value);
	}

	public boolean containsDocument(String key, String value){
		return this.mongo.containsDocument(key, value);
	}
	
	public void updateDocument(String collection, DBObject alter, DBObject searchQuery) {
		this.mongo.setCollection(collection);
		
		this.updateDocument(alter, searchQuery);
	}
	
	public void updateDocument(DBObject alter, DBObject searchQuery) {
		this.mongo.updateDocument(alter, searchQuery);
	}

	public void removeDocument(DBObject query) {
		this.mongo.removeDocument(query);
		
	}
}
