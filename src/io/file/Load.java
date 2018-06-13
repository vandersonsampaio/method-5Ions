package io.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import core.entity.EntitySentiment;
import core.entity.ExternalData;
import util.commom.Dates;

public class Load {
	private static final String FILENAME = "C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\DW_Alemao_Trans\\";

	public JSONArray getDocuments() {
		return this.getDocuments(FILENAME);
	}
	
	@SuppressWarnings("unchecked")	
	public JSONArray getDocuments(String fileName) {
		BufferedReader br = null;
		FileReader fr = null;
		JSONArray arJSON = new JSONArray();
		JSONObject json = null;

		File folder = new File(fileName);

		for (File file : folder.listFiles()) {

			try {

				fr = new FileReader(fileName + file.getName());
				br = new BufferedReader(fr);

				StringBuilder str = new StringBuilder();
				String sCurrentLine;

				while ((sCurrentLine = br.readLine()) != null) {
					str.append(sCurrentLine);
				}

				JSONParser parser = new JSONParser();
				json = (JSONObject) parser.parse(str.toString());
				json.put("fileName", file.getName());

				if(!json.get("text").toString().equals(""))
					arJSON.add(json);

			} catch (IOException | ParseException e) {
				e.printStackTrace();

			} finally {
				try {
					if (br != null)
						br.close();

					if (fr != null)
						fr.close();

				} catch (IOException ex) {
					ex.printStackTrace();

				}
			}
		}

		return arJSON;

	}

	@SuppressWarnings("unchecked")
	public JSONArray getEntitiesSentiment() {
		String FILENAME = "C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\EntidadeSentimentos\\DW_Alemao_Trans_Sum\\Luhn";

		JSONArray arJSON = new JSONArray();
		File folder = new File(FILENAME);

		BufferedReader br = null;
		FileReader fr = null;
		JSONObject json = null;

		for (File file : folder.listFiles()) {
			try {
				fr = new FileReader(FILENAME + File.separator + file.getName());
				br = new BufferedReader(fr);

				StringBuilder str = new StringBuilder();
				String sCurrentLine;

				while ((sCurrentLine = br.readLine()) != null) {
					str.append(sCurrentLine);
				}

				JSONParser parser = new JSONParser();
				json = (JSONObject) parser.parse(str.toString());
				json.put("fileName", file.getName());
				arJSON.add(json);
			} catch (IOException | ParseException e) {
				e.printStackTrace();

			} finally {
				try {
					if (br != null)
						br.close();

					if (fr != null)
						fr.close();

				} catch (IOException ex) {
					ex.printStackTrace();

				}
			}
		}
		
		return arJSON;
	}

	public Hashtable<Long, EntitySentiment> getSerialTime(String name) {
		BufferedReader br = null;
		FileReader fr = null;

		Hashtable<Long, EntitySentiment> ret = new Hashtable<>();

		try {
			fr = new FileReader(name);
			br = new BufferedReader(fr);

			String sCurrentLine;

			EntitySentiment auxEntity;
			while ((sCurrentLine = br.readLine()) != null) {
				auxEntity = new EntitySentiment();
				String[] parts = sCurrentLine.split(";");

				auxEntity.setEntityName(name);
				
				if(!parts[2].isEmpty())
					auxEntity.setPositiveSentimentDM(Float.parseFloat(parts[2].replaceAll(",", ".")));
				else
					auxEntity.setPositiveSentimentDM(0);
				
				if(!parts[3].isEmpty())
					auxEntity.setNegativaSentimentDM(Float.parseFloat(parts[3].replaceAll(",", ".")));
				else
					auxEntity.setNegativaSentimentDM(0);
				
				if(!parts[1].isEmpty())
					auxEntity.setNumberDirectMentions(Integer.parseInt(parts[1]));
				else
					auxEntity.setNumberDirectMentions(0);
				
				if(!parts[4].isEmpty())
					auxEntity.setNumberCoMentions(Integer.parseInt(parts[4]));
				else
					auxEntity.setNumberCoMentions(0);
				
				
				if(!parts[5].isEmpty())
					auxEntity.setPositiveSentimentCM(Float.parseFloat(parts[5].replaceAll(",", ".")));
				else
					auxEntity.setPositiveSentimentCM(0);
				
				if(!parts[6].isEmpty())
					auxEntity.setNegativaSentimentCM(Float.parseFloat(parts[6].replaceAll(",", ".")));
				else
					auxEntity.setNegativaSentimentCM(0);

				ret.put(Dates.dateTime(parts[0]).getMillis(), auxEntity);
			}

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {
				ex.printStackTrace();

			}
		}
		return ret;
	}

	public double[] getSerialTimeCompact(String path, String fileName, Integer index) {
		String FILENAME = path + File.separator + fileName;

		BufferedReader br = null;
		FileReader fr = null;

		Hashtable<Integer, Float> htAux = new Hashtable<>();

		try {
			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				String[] parts = sCurrentLine.split(";");

				if(parts.length > 1)
					htAux.put(Integer.parseInt(parts[0]), Float.parseFloat(parts[index]));
			}

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {
				ex.printStackTrace();

			}
		}
		
		if(htAux.size() > 0) {
			double[] ret = new double[htAux.size()];
			
			for(int i = 0; i < ret.length; i++) {
				ret[i] = htAux.get(i + 1);
			}
			
			return ret;
		}
			
		return null;
	}

	public List<ExternalData> getExternalData(String fileName){
		BufferedReader br = null;
		FileReader fr = null;

		List<ExternalData> listExtData = new ArrayList<>();

		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);

			String sCurrentLine;

			ExternalData extData;
			while ((sCurrentLine = br.readLine()) != null) {
				extData = new ExternalData();
				String[] parts = sCurrentLine.split(";");

				extData.setPublicationDate(Dates.dateTime(parts[0]));
				
				if(!parts[2].isEmpty())
					extData.setInitialDate(Dates.dateTime(parts[1]));
				
				extData.setEndDate(Dates.dateTime(parts[2]));
				extData.setValue(Float.parseFloat(parts[3]));
				
				listExtData.add(extData);
			}

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {
				ex.printStackTrace();

			}
		}
		
		Collections.sort(listExtData, new Comparator<ExternalData>() {
			@Override
			public int compare(ExternalData o1, ExternalData o2) {
				return o1.getPublicationDate().equals(o2.getPublicationDate()) ? 0 :
					o1.getPublicationDate().isBefore(o2.getPublicationDate()) ? -1 : 
						1;
			}
		});
		return listExtData;
	}

	public List<DateTime> getEndDateED(String fileName){
		List<ExternalData> listExtData = this.getExternalData(fileName);
		List<DateTime> listDates = new ArrayList<>();
		
		
		for(ExternalData extData : listExtData){
			listDates.add(extData.getEndDate());
		}
		
		Collections.sort(listDates);
		
		return listDates;
	}
}
