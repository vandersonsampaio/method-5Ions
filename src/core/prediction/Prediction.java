package core.prediction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import core.correlation.CalculateWeight;
import io.db.LoadDocuments;
import util.commom.Dates;
import weka.core.Instances;

public class Prediction {
	private String host;
	private String databaseName;
	private String collection;
	private String collectionExternalFile;
	
	public Prediction(String host, String databaseName, String collection, String collectionExternalFile) {
		this.host = host;
		this.databaseName = databaseName;
		this.collection = collection;
		this.collectionExternalFile = collectionExternalFile;
	}
	
	public void calculePrediction(String nameTarget, String nameExternalIndicator, int generations, double criteria) throws Exception {
		LoadDocuments ld = new LoadDocuments(host, databaseName, collectionExternalFile);
		
		BasicDBObject fields = new BasicDBObject("_id", "1").append("entity", "1").append("type", "1").append("metric", "1");

		// Entidades para análise
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

		// Pega as metrics calculadas
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

		// Pegar a medida externa
		BasicDBObject externalIndicator = ld
				.findOne(collectionExternalFile, new BasicDBObject().append("name", nameExternalIndicator).append("is_indicator", true));
		BasicDBList ltValues = (BasicDBList) externalIndicator.get("values");
		Hashtable<Date, Double> htExternal = new Hashtable<>();
		
		for(int i = 0; i < ltValues.size(); i++) {
			htExternal.put( Dates.getZeroTimeDate(((BasicDBObject) ltValues.get(i)).getDate("date")), 
					((BasicDBObject) ltValues.get(i)).getDouble("value"));
		}
		
		for(int i = 0; i < entitiesAnalysis.size(); i++){
			double accuracy = this.calculateAccuracy((BasicDBObject) entitiesAnalysis.get(i), htExternal);
			
			System.out.println("Entity: " + ((BasicDBObject) entitiesAnalysis.get(i)).getString("entity")
					+ " Accuracy Prediction: " + accuracy);
		}

		// Joga tudo no algoritmo genético
		CalculateWeight.correlation = false;
		CalculateWeight cw = new CalculateWeight(generations, criteria);
		cw.calculateWeigth(entitiesAnalysis, htExternal);
	}
	
	public double calculateAccuracy(BasicDBObject entity, Hashtable<Date, Double> dataExt) throws Exception{
		Date maxDate;
		Date minDate = maxDate = dataExt.keys().nextElement();

		for (Date d : dataExt.keySet()) {
			if (d.before(minDate))
				minDate = d;
			else if (d.after(maxDate))
				maxDate = d;
		}

		double[] entityMetric = new double[dataExt.size()];
		double[] indicatorExternal = new double[dataExt.size()];

		Date d = minDate;
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		
		int indexDate = 0;
		while (d.before(maxDate) || d.equals(maxDate)) {
			if (dataExt.containsKey(d)) {
				indicatorExternal[indexDate] = dataExt.get(d);

				entityMetric[indexDate] = 0;
				BasicDBList metric = (BasicDBList) ((BasicDBObject) entity.get("metric")).get("values_acum");

				for (int k = 0; k < metric.size(); k++) {
					if (Dates.getZeroTimeDate(((BasicDBObject) metric.get(k)).getDate("date")).equals(Dates.getZeroTimeDate(d))) {
						entityMetric[indexDate] = ((BasicDBObject) metric.get(k)).getDouble("value_direct");
						break;
					}
				}
				indexDate++;
			}

			c.add(Calendar.DATE, 1);
			d = Dates.getZeroTimeDate(c.getTime());
		}
		
		return this.calculateAccuracy(entityMetric, indicatorExternal);
	}
	
	private double calculateAccuracy(double[] data, double[] indicatorExternal) throws Exception{
		AdapterWeka aw = new AdapterWeka();
		Instances insts = aw.createInstances(data, indicatorExternal);
		return aw.evaluation(aw.createClassifier(insts), insts);
	}
}
