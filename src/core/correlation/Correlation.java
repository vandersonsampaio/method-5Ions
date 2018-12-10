package core.correlation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import core.entity.ExternalData;
import io.db.LoadDocuments;
import io.file.Load;
import util.adjustWeight.AG.Engine;
import util.commom.Dates;
import util.commom.Properties;

public class Correlation {

	private String host;
	private String databaseName;
	private String collection;
	private String collectionExternalFile;

	public Correlation() {
		// this.coPearson = new double[6];
	}

	public Correlation(String host, String databaseName, String collection, String collectionExternalFile) {
		this.host = host;
		this.databaseName = databaseName;
		this.collection = collection;
		this.collectionExternalFile = collectionExternalFile;
	}

	public void calculeCorrelation(String nameTarget, String nameExternalIndicator, int generations, double criteria) throws Exception {
		LoadDocuments ld = new LoadDocuments(host, databaseName, collectionExternalFile);
		
		BasicDBObject fields = new BasicDBObject("_id", "1").append("entity", "1").append("type", "1").append("metric", "1");

		// Definir as entidades para análise
		BasicDBObject externalTarget = ld
				.findOne(new BasicDBObject().append("name", nameTarget).append("is_target", true));
		BasicDBObject entityTarget = (BasicDBObject) externalTarget.get("values");
		
		BasicDBList entitiesRelation = (BasicDBList) entityTarget.get("relations");

		BasicDBObject query = new BasicDBObject();
		List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
		obj.add(new BasicDBObject("entity", entityTarget.getString("name")));
		obj.add(new BasicDBObject("type", entityTarget.getString("type")));
		query.put("$and", obj);

		BasicDBObject entity = ld.findOne(collection, query, fields);

		// Pegar as metrics calculadas
		BasicDBList entitiesAnalysis = new BasicDBList();

		if (entity != null)
			entitiesAnalysis.add(entity);
		else
			throw new Exception("Target was not defined");

		for (int i = 0; i < entitiesRelation.size(); i++) {
			query.clear();

			obj = new ArrayList<BasicDBObject>();
			obj.add(new BasicDBObject("entity", ((BasicDBObject) entitiesRelation.get(i)).getString("name")));
			obj.add(new BasicDBObject("type", ((BasicDBObject) entitiesRelation.get(i)).getString("type")));
			query.put("$and", obj);

			BasicDBObject search = ld.findOne(query, fields);

			if (search != null)
				entitiesAnalysis.add(search);
		}

		// Pegar a medida externa para correlação
		BasicDBObject externalIndicator = ld
				.findOne(collectionExternalFile, new BasicDBObject().append("name", nameExternalIndicator).append("is_indicator", true));
		BasicDBList ltValues = (BasicDBList) externalIndicator.get("values");
		Hashtable<Date, Double> htExternal = new Hashtable<>();
		
		for(int i = 0; i < ltValues.size(); i++) {
			htExternal.put( Dates.getZeroTimeDate(((BasicDBObject) ltValues.get(i)).getDate("date")), 
					((BasicDBObject) ltValues.get(i)).getDouble("value"));
		}
		

		// Jogar tudo no algoritmo genético
		CalculateWeight.correlation = true;
		CalculateWeight cw = new CalculateWeight(generations, criteria);
		cw.calculateWeigth(entitiesAnalysis, htExternal);
	}

	public double generationCorrelation(double[] datas) {
		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

		return Math.abs(pearsonsCorrelation.correlation(datas, Engine.indicatorExternal));
	}
	
	public double generationCorrelation(Set<String> names, Hashtable<String, double[]> datas) {
		List<String> namesSorted = names.stream().collect(Collectors.toList());
		Collections.sort(namesSorted, (o1, o2) -> o1.compareTo(o2));

		Load load = new Load();
		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

		for (String name : namesSorted) {
			double[] data = datas.get(name);
			double[] inExt = new double[data.length];

			List<ExternalData> listExtData = load
					.getExternalData(Properties.getProperty("fileExternalData") + File.separator + name + ".csv");

			for (int i = 0; i < listExtData.size(); i++) {
				inExt[i] = listExtData.get(i).getValue();
			}

			return Math.abs(pearsonsCorrelation.correlation(data, inExt));

		}

		return 0;
	}

}
