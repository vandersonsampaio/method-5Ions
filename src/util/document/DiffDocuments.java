package util.document;

import io.file.Load;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DiffDocuments {

	private Hashtable<String, String> htDocuments;
	private List<String> lsUrls;
	private final String PATHSOURCE = "C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\DW_Alemao2\\AFD\\[P] Alternative for Germany\\";
	private final String PATHTARGET = "C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\DW_Alemao2\\CDU\\[P] Christian Democratic Union of Germany\\";
	
	public DiffDocuments(){
		this.htDocuments = new Hashtable<>();
		this.lsUrls = new ArrayList<String>();
	}
		
	public boolean loadDocuments(){
		Load load = new Load();
		
		JSONArray documentsSource = load.getDocuments(PATHSOURCE);
		JSONArray documentsTarget = load.getDocuments(PATHTARGET);
		
		for(int i = 0; i < documentsSource.size(); i++){
			JSONObject joSource = (JSONObject) documentsSource.get(i);
			
			lsUrls.add(joSource.get("url").toString());
		}
		
		for(int i = 0; i < documentsTarget.size(); i++){
			JSONObject joTarget = (JSONObject) documentsTarget.get(i);
			
			htDocuments.put(joTarget.get("url").toString(), joTarget.get("fileName").toString());
		}
		
		return true;
	}
	
	public boolean removeDiffs(){
		if(PATHSOURCE.equals(PATHTARGET)) {
			System.out.println("Mesmos caminhos!!!");
			System.exit(0);
		}
		
		int j = 0;
		for(int i = lsUrls.size() - 1; i >= 0; i--) {
			if(htDocuments.containsKey(lsUrls.get(i))) {
				//System.out.println("Duplicado: " + lsUrls.get(i) + " " + htDocuments.get(lsUrls.get(i)));
				
				File file = new File(PATHTARGET + htDocuments.get(lsUrls.get(i)));
				
				if(file.delete()) {
					System.out.println("Removido!");
					j++;
				}
				else
					System.out.println("Erro!");
				
				htDocuments.remove(lsUrls.get(i));
			}
		}
		
		System.out.println("Total removido: " + j);
		
		return true;
	}
	
	public static void main(String[] args){
		DiffDocuments diff = new DiffDocuments();
		diff.loadDocuments();
		diff.removeDiffs();
	}
}
