package io.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import core.entity.EntitySentiment;
import util.commom.Dates;

public class Save {
	
	private String path;
	private String extension;
	private String fileName;

	public void setPath(String path) {
		this.path = path;
	}
	
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String save(JSONArray array) {

		StringBuilder retorno = new StringBuilder();

		for (int i = 0; i < array.size(); i++) {
			this.save((JSONObject) array.get(i));
		}
		return retorno.toString();
	}

	public String save(JSONObject object) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {
			String content = object.toJSONString();
			
			fw = new FileWriter(path + File.separator + this.fileName + "." + extension);
			bw = new BufferedWriter(fw);
			bw.write(content);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return "";
	}

	public String saveSerialTimeCSV(Hashtable<Long, Hashtable<String, EntitySentiment>> elements, Hashtable<Long, Integer> numbersDocs, Hashtable<Long, Integer> lengthTexts) {
		Hashtable<String, Hashtable<Long, EntitySentiment>> htSave = new Hashtable<>();
		Set<String> names = new HashSet<String>();

		for (Long key : elements.keySet()) {
			names.addAll(elements.get(key).keySet());
		}

		for (Long key : elements.keySet()) {
			Hashtable<String, EntitySentiment> entity = elements.get(key);

			for (String name : names) {
				Hashtable<Long, EntitySentiment> auxTimeEntity;

				if (htSave.containsKey(name)) {
					auxTimeEntity = htSave.get(name);
				} else {
					auxTimeEntity = new Hashtable<>();
				}

				EntitySentiment sentiment = null;
				if (entity.containsKey(name))
					sentiment = entity.get(name);
				else {
					sentiment = new EntitySentiment();
					sentiment.setEntityName(name);
				}

				auxTimeEntity.put(key, sentiment);
				htSave.put(name, auxTimeEntity);
			}
		}

		saveCSV(htSave, numbersDocs, lengthTexts);

		return "";
	}

	private void saveCSV(Hashtable<String, Hashtable<Long, EntitySentiment>> table, Hashtable<Long, Integer> numbersDocs, Hashtable<Long, Integer> lengthTexts) {
		StringBuilder str = null;

		for (String name : table.keySet()) {
			str = new StringBuilder();
			
			Set<Long> keySet = table.get(name).keySet();
			
			for (Long date : (Long[]) keySet.stream().sorted().toArray()) {
				EntitySentiment sentiment = table.get(name).get(date);

				str.append(Dates.formatDateTime(date));
				str.append(";");
				
				if(!numbersDocs.containsKey(date)){
					str.append(";;;;;;0;;\n");
				} else {
					str.append(sentiment.getNumberDirectMentions());
					str.append(";");
					str.append(String.format("%f", sentiment.getPositiveSentimentDM()));
					str.append(";");
					str.append(String.format("%f", sentiment.getNegativaSentimentDM()));
					str.append(";");
					str.append(sentiment.getNumberCoMentions());
					str.append(";");					
					str.append(String.format("%f", sentiment.getPositiveSentimentCM()));
					str.append(";");
					str.append(String.format("%f", sentiment.getNegativaSentimentCM()));
					str.append(";");
					str.append(numbersDocs.get(date));
					str.append(";");
					str.append(lengthTexts.get(date));
					str.append(";\n");	
				}
			}
			
			this.setExtension("csv");
			save(str.toString(), name);
		}
	}
	
	public String save(String text, String name) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {
			fw = new FileWriter(path + File.separator + name.replace("?", "").replace("\\", "").replace("/", "") + "." + extension);
			bw = new BufferedWriter(fw);
			bw.write(text);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return "";
	}

	public void summarizationMetrics(Hashtable<Integer, Hashtable<String, Float>> serialTimeResult, String fileName) {
		StringBuilder str = new StringBuilder();
		
		for(Integer i : serialTimeResult.keySet()) {
			str.append(i);
			str.append(";");
			str.append(serialTimeResult.get(i).get("s1"));
			str.append(";");
			str.append(serialTimeResult.get(i).get("s2"));
			str.append(";");
			str.append(serialTimeResult.get(i).get("s3"));
			str.append(";");
			str.append(serialTimeResult.get(i).get("s4"));
			str.append(";\n");
		}
		
		save(str.toString(), fileName);
	}
}
