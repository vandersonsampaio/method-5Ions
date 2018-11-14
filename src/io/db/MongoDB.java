package io.db;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoDB {
	
	private final String PORT = "27017";
	private String urlConnection;
	private	MongoClient mongoClient;
	private DB database;
	private DBCollection collection;
	
	public MongoDB(String urlConnection) throws UnknownHostException{
		this.collection = null;
		this.database = null;
		this.urlConnection = urlConnection;
		this.createConnection();
	}
	
	private void createConnection() throws UnknownHostException{	
		mongoClient = new MongoClient(new MongoClientURI("mongodb://" + urlConnection + ":" + PORT));
	}
	
	public void setDataBase(String databaseName){
		this.database = mongoClient.getDB(databaseName);
	}
	
	public void setCollection(String collectionName){
		this.collection = database.getCollection(collectionName);
	}
	
	
	public void insertDocument(DBObject obj){
		this.collection.insert(obj);
	}
	
	public DBObject getDocument(String id){
		DBObject query = new BasicDBObject("_id", id);
		DBCursor cursor = collection.find(query);
		
		if(cursor.length() > 0)
			return cursor.one();
		
		return null;
	}
	
	public List<DBObject> findAll(){
		DBCursor cursor = collection.find();
		
		if(cursor.length() > 0)
			return cursor.toArray();
		
		return new ArrayList<DBObject>();
	}
	
	public List<DBObject> findByQuery(DBObject query, int limit){
		DBCursor cursor = collection.find(query).limit(limit);
		
		if(cursor.length() > 0)
			return cursor.toArray();
		
		return new ArrayList<DBObject>();
	}
	
	public List<DBObject> findByQuery(DBObject query){
		DBCursor cursor = collection.find(query);
		
		if(cursor.length() > 0)
			return cursor.toArray();
		
		return new ArrayList<DBObject>();
	}

	public boolean containsDocument(String key, String value) {
		DBCursor cursor = collection.find(new BasicDBObject().append(key, value));
		
		return cursor.count() > 0;
	}
	
	public void updateDocument(DBObject alter, DBObject query){
		collection.update(alter, query);
	}
}
