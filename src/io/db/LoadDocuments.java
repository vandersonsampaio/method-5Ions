package io.db;

import java.net.UnknownHostException;
import java.util.List;

import org.json.simple.JSONArray;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class LoadDocuments {

	private MongoDB mongo;
	
	public static void main(String[] args){
		try {
			LoadDocuments ld = new LoadDocuments("", "db_poll_fakenews");
			
			System.out.println(ld.getAllDocuments("documents").size());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public LoadDocuments(String url, String database) throws UnknownHostException{
		this.mongo = new MongoDB(url);
		this.mongo.setDataBase(database);
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getAllDocuments(String collection){
		JSONArray ret = new JSONArray();
		
		this.mongo.setCollection(collection);
		
		List<DBObject> result = mongo.findAll();
		
		for(DBObject obj : result){
			ret.add(JSON.parse(obj.toString()));
		}
		
		return ret;
	}
}
