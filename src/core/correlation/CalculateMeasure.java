package core.correlation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import io.db.LoadDocuments;
import io.db.SaveDocuments;

public class CalculateMeasure implements Runnable {

	private final int NUMBERTHREAD = 5;
	private JSONArray arr;
	private String host;
	private String databaseName;
	private String collection;

	public CalculateMeasure(String host, String databaseName, String collection) {
		this.host = host;
		this.databaseName = databaseName;
		this.collection = collection;
		this.arr = null;
	}

	public CalculateMeasure(String host, String databaseName, String collection, JSONArray arr) {
		this.host = host;
		this.databaseName = databaseName;
		this.collection = collection;
		this.arr = arr;
	}

	@SuppressWarnings("unchecked")
	public boolean summarizationMetric() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
			UnknownHostException, InterruptedException {

		// Carregar todas as entidades que sofreram atualização na serial_time
		LoadDocuments ld = new LoadDocuments(host, databaseName, collection);

		BasicDBObject query = new BasicDBObject();
		List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
		obj.add(new BasicDBObject("is_serialtime", "true"));
		obj.add(new BasicDBObject("is_calculatemeasure", "false"));
		query.put("$and", obj);

		JSONArray jarrEntities = ld.findByQuery(query, 500);
System.out.println("Consultou");
		int length = jarrEntities.size() / NUMBERTHREAD;

		if (length == 0)
			return true;

		Thread[] tr = new Thread[NUMBERTHREAD];
		for (int i = 0; i < NUMBERTHREAD; i++) {
			List<BasicDBObject> subList = jarrEntities.subList(length * i,
					i + 1 < NUMBERTHREAD ? length * (i + 1) : jarrEntities.size());

			JSONArray slJarr = new JSONArray();
			for (int du = 0; du < subList.size(); du++) {
				slJarr.add((BasicDBObject) subList.get(du));
			}

			CalculateMeasure cm = new CalculateMeasure(host, databaseName, collection, slJarr);

			tr[i] = new Thread(cm);
			tr[i].start();
		}

		boolean isAlive = true;
		while (isAlive) {
			Thread.sleep(5000);
			System.out.println("Calculate Measure is alive!");

			isAlive = false;
			for (int i = 0; i < NUMBERTHREAD; i++)
				isAlive = isAlive || tr[i].isAlive();
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	private void splitDay(BasicDBObject entity)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, UnknownHostException {

		SaveDocuments sd = new SaveDocuments(host, databaseName, collection);
		// serialTimeShort
		// SerialTimeAcum
		if (!entity.containsKey("serial_time_short") || entity.get("serial_time_short") == null)
			return;

		Class<?> metricClass = Class.forName("core.summarizationmetric.Metric");

		Object metricObject = metricClass.newInstance();
		Method calculateMethod = metricClass.getMethod("calculatedMetric", double.class, double.class);

		BasicDBList ltCalculeShort = new BasicDBList();
		BasicDBList stShort = ((BasicDBList) entity.get("serial_time_short"));
		for (int du = 0; du < stShort.size(); du++) {
			// sumarize_metric: {metric: , name: '', description: '', [{date: '',
			// value_direct: '', value_coref: '', value_total: ''}]}

			Double value_direct = null;
			Double value_coref = null;
			Double value_total = null;
			BasicDBObject sentiment = (BasicDBObject) ((BasicDBObject) stShort.get(du)).get("sentiments");

			if (sentiment != null) {
				value_direct = (Double) calculateMethod.invoke(metricObject, sentiment.getDouble("score_direct_pos"),
						sentiment.getDouble("score_direct_neg"));
				value_coref = (Double) calculateMethod.invoke(metricObject, sentiment.getDouble("score_coref_pos"),
						sentiment.getDouble("score_coref_neg"));
				value_total = (Double) calculateMethod.invoke(metricObject,
						sentiment.getDouble("score_direct_pos") + sentiment.getDouble("score_coref_pos"),
						sentiment.getDouble("score_direct_neg") + sentiment.getDouble("score_coref_neg"));
			}

			ltCalculeShort.add(new BasicDBObject().append("date", ((BasicDBObject) stShort.get(du)).getDate("date"))
					.append("value_direct", value_direct).append("value_coref", value_coref)
					.append("value_total", value_total));
		}
		
		BasicDBList ltCalculeAcum = new BasicDBList();
		BasicDBList stAcum = ((BasicDBList) entity.get("serial_time_acum"));
		for (int du = 0; du < stAcum.size(); du++) {
			
			Double value_direct = null;
			Double value_coref = null;
			Double value_total = null;
			BasicDBObject sentiment = (BasicDBObject) ((BasicDBObject) stAcum.get(du)).get("sentiments");

			if (sentiment != null) {
				value_direct = (Double) calculateMethod.invoke(metricObject, sentiment.getDouble("score_direct_pos"),
						sentiment.getDouble("score_direct_neg"));
				value_coref = (Double) calculateMethod.invoke(metricObject, sentiment.getDouble("score_coref_pos"),
						sentiment.getDouble("score_coref_neg"));
				value_total = (Double) calculateMethod.invoke(metricObject,
						sentiment.getDouble("score_direct_pos") + sentiment.getDouble("score_coref_pos"),
						sentiment.getDouble("score_direct_neg") + sentiment.getDouble("score_coref_neg"));
			}

			ltCalculeAcum.add(new BasicDBObject().append("date", ((BasicDBObject) stAcum.get(du)).getDate("date"))
					.append("value_direct", value_direct).append("value_coref", value_coref)
					.append("value_total", value_total));
		}

		sd.updateDocument(
				new BasicDBObject().append("$set", new BasicDBObject().append("is_calculatemeasure", "true")
						.append("metric", new BasicDBObject().append("name", "f2").append("description", "division pos plus neg and neg")
						.append("values_short", ltCalculeShort).append("values_acum", ltCalculeAcum))),
				new BasicDBObject().append("_id", entity.get("_id")));
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < arr.size(); i++) {

				//splitDay((BasicDBObject) arr.get(i));
				BasicDBObject entity = (BasicDBObject) arr.get(i);
				SaveDocuments sd = new SaveDocuments(host, databaseName, collection);
				// serialTimeShort
				// SerialTimeAcum
				if (!entity.containsKey("serial_time_short") || entity.get("serial_time_short") == null) {
					sd.updateDocument(
							new BasicDBObject().append("$set", new BasicDBObject().append("is_calculatemeasure", "maybe")),
							new BasicDBObject().append("_id", entity.get("_id")));
					return;
				}

				Class<?> metricClass = Class.forName("core.summarizationmetric.Metric");

				Object metricObject = metricClass.newInstance();
				Method calculateMethod = metricClass.getMethod("calculatedMetric", double.class, double.class);

				BasicDBList ltCalculeShort = new BasicDBList();
				BasicDBList stShort = ((BasicDBList) entity.get("serial_time_short"));
				for (int du = 0; du < stShort.size(); du++) {
					// sumarize_metric: {metric: , name: '', description: '', [{date: '',
					// value_direct: '', value_coref: '', value_total: ''}]}

					Double value_direct = null;
					Double value_coref = null;
					Double value_total = null;
					BasicDBObject sentiment = (BasicDBObject) ((BasicDBObject) stShort.get(du)).get("sentiments");

					if (sentiment != null) {
						value_direct = (Double) calculateMethod.invoke(metricObject, sentiment.getDouble("score_direct_pos"),
								sentiment.getDouble("score_direct_neg"));
						value_coref = (Double) calculateMethod.invoke(metricObject, sentiment.getDouble("score_coref_pos"),
								sentiment.getDouble("score_coref_neg"));
						value_total = (Double) calculateMethod.invoke(metricObject,
								sentiment.getDouble("score_direct_pos") + sentiment.getDouble("score_coref_pos"),
								sentiment.getDouble("score_direct_neg") + sentiment.getDouble("score_coref_neg"));
					}

					ltCalculeShort.add(new BasicDBObject().append("date", ((BasicDBObject) stShort.get(du)).getDate("date"))
							.append("value_direct", value_direct).append("value_coref", value_coref)
							.append("value_total", value_total));
				}
				
				BasicDBList ltCalculeAcum = new BasicDBList();
				BasicDBList stAcum = ((BasicDBList) entity.get("serial_time_acum"));
				for (int du = 0; du < stAcum.size(); du++) {
					
					Double value_direct = null;
					Double value_coref = null;
					Double value_total = null;
					BasicDBObject sentiment = (BasicDBObject) ((BasicDBObject) stAcum.get(du)).get("sentiments");

					if (sentiment != null) {
						value_direct = (Double) calculateMethod.invoke(metricObject, sentiment.getDouble("score_direct_pos"),
								sentiment.getDouble("score_direct_neg"));
						value_coref = (Double) calculateMethod.invoke(metricObject, sentiment.getDouble("score_coref_pos"),
								sentiment.getDouble("score_coref_neg"));
						value_total = (Double) calculateMethod.invoke(metricObject,
								sentiment.getDouble("score_direct_pos") + sentiment.getDouble("score_coref_pos"),
								sentiment.getDouble("score_direct_neg") + sentiment.getDouble("score_coref_neg"));
					}

					ltCalculeAcum.add(new BasicDBObject().append("date", ((BasicDBObject) stAcum.get(du)).getDate("date"))
							.append("value_direct", value_direct).append("value_coref", value_coref)
							.append("value_total", value_total));
				}

				sd.updateDocument(
						new BasicDBObject().append("$set", new BasicDBObject().append("is_calculatemeasure", "true")
								.append("metric", new BasicDBObject().append("name", "f2").append("description", "division pos plus neg and neg")
								.append("values_short", ltCalculeShort).append("values_acum", ltCalculeAcum))),
						new BasicDBObject().append("_id", entity.get("_id")));
				
				// splitMonth((BasicDBObject) jarrEntities.get(i));
				// splitWeek((BasicDBObject) jarrEntities.get(i));
				// Consulta a collection PERIOD no banco de dados e realiza calculo
				// de metrica para todos
				// splitCustom((BasicDBObject) jarrEntities.get(i));
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException | UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
