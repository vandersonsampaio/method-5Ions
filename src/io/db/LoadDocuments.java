package io.db;

import java.net.UnknownHostException;
import java.util.List;

import org.json.simple.JSONArray;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class LoadDocuments {

	private MongoDB mongo;
	
	public static void main(String[] args){
		try {
			LoadDocuments ld = new LoadDocuments("localhost", "db_news_brazil", "documents");
			
			System.out.println(ld.getAllDocuments().size());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public LoadDocuments(String url, String database, String collection) throws UnknownHostException{
		this.mongo = new MongoDB(url);
		this.mongo.setDataBase(database);
		this.mongo.setCollection(collection);
	}
	
	public JSONArray getAllDocuments(String collection){
		this.mongo.setCollection(collection);
		
		return this.getAllDocuments();
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getAllDocuments(){
		JSONArray ret = new JSONArray();
				
		List<DBObject> result = mongo.findAll();
		
		for(DBObject obj : result){
			ret.add(JSON.parse(obj.toString()));
		}
		
		return ret;
	}
	
	public JSONArray findByQuery(String collection, DBObject query) {
		this.mongo.setCollection(collection);
		
		return this.findByQuery(query);
	}

	@SuppressWarnings("unchecked")
	public JSONArray findByQuery(DBObject query) {
		JSONArray ret = new JSONArray();
		
		List<DBObject> result = mongo.findByQuery(query);
		
		for(DBObject obj : result){
			ret.add(JSON.parse(obj.toString()));
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray findByQuery(DBObject query, int limit) {
		JSONArray ret = new JSONArray();
		
		List<DBObject> result = mongo.findByQuery(query, limit);
		
		for(DBObject obj : result){
			ret.add(JSON.parse(obj.toString()));
		}
		
		return ret;
	}

	public BasicDBObject findOne(DBObject query) {
		return mongo.findOne(query);
	}
	
	public BasicDBObject findOne(DBObject query, DBObject fields) {
		return mongo.findOne(query, fields);
	}
	
	public BasicDBObject findOne(String collection, DBObject query) {
		this.mongo.setCollection(collection);
		
		return this.findOne(query);
	}
	
	public BasicDBObject findOne(String collection, DBObject query, DBObject fields) {
		this.mongo.setCollection(collection);
		
		return this.findOne(query, fields);
	}
}
