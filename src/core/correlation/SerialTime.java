package core.correlation;

import java.io.File;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import core.entity.EntitySentiment;
import core.entity.SumarySentiment;
import io.db.LoadDocuments;
import io.db.SaveDocuments;
import io.file.Load;
import io.file.Save;
import util.commom.Dates;
import util.commom.Properties;

public class SerialTime {

	private Hashtable<Long, List<EntitySentiment>> entitiesTime;
	private Hashtable<Long, Integer> numbersDocs;
	private Hashtable<Long, Integer> lengthTexts;
	private String host;
	private String databaseName;
	private String collection;

	public SerialTime() {
		this.entitiesTime = new Hashtable<>();
		this.numbersDocs = new Hashtable<Long, Integer>();
		this.lengthTexts = new Hashtable<Long, Integer>();
	}

	public SerialTime(String host, String databaseName, String collection) {
		this.entitiesTime = new Hashtable<>();
		this.numbersDocs = new Hashtable<Long, Integer>();
		this.lengthTexts = new Hashtable<Long, Integer>();

		this.host = host;
		this.databaseName = databaseName;
		this.collection = collection;
	}

	public Hashtable<Long, Hashtable<String, EntitySentiment>> generationSerie(DateTime inicialDate,
			DateTime finalDate) {

		Hashtable<Long, Hashtable<String, EntitySentiment>> ret = new Hashtable<>();

		DateTime dtAux = inicialDate;
		for (; dtAux.isBefore(finalDate) || dtAux.isEqual(finalDate);) {
			List<EntitySentiment> entities = this.entitiesTime.get(dtAux.getMillis());

			Hashtable<String, EntitySentiment> htAux = new Hashtable<>();
			for (int j = 0; entities != null && j < entities.size(); j++) {
				EntitySentiment element = entities.get(j);
				String name = element.getEntityName();

				if (htAux.containsKey(name)) {
					EntitySentiment aux = htAux.get(name);

					aux.setNegativaSentimentDM(aux.getNegativaSentimentDM() + element.getNegativaSentimentDM());
					aux.setPositiveSentimentDM(aux.getPositiveSentimentDM() + element.getPositiveSentimentDM());
					aux.setNegativaSentimentCM(aux.getNegativaSentimentCM() + element.getNegativaSentimentCM());
					aux.setPositiveSentimentCM(aux.getPositiveSentimentCM() + element.getPositiveSentimentCM());
					aux.setNumberCoMentions(aux.getNumberCoMentions() + element.getNumberCoMentions());
					aux.setNumberDirectMentions(aux.getNumberDirectMentions() + element.getNumberDirectMentions());
				} else {
					EntitySentiment aux = new EntitySentiment();

					aux.setEntityName(name);
					aux.setNegativaSentimentDM(element.getNegativaSentimentDM());
					aux.setPositiveSentimentDM(element.getPositiveSentimentDM());
					aux.setNegativaSentimentCM(element.getNegativaSentimentCM());
					aux.setPositiveSentimentCM(element.getPositiveSentimentCM());
					aux.setNumberCoMentions(element.getNumberCoMentions());
					aux.setNumberDirectMentions(element.getNumberDirectMentions());

					htAux.put(name, aux);
				}
			}

			ret.put(dtAux.getMillis(), htAux);
			dtAux = dtAux.plusDays(1);
		}

		Save save = new Save();
		save.setPath(
				"C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\SerialTime\\DW_Alemao_Trans_Sum\\Luhn");
		save.saveSerialTimeCSV(ret, numbersDocs, lengthTexts);

		return ret;
	}

	public Hashtable<Integer, Hashtable<String, Float>> summarizationMetric(DateTime dtInitial, DateTime dtEnd,
			Hashtable<Long, EntitySentiment> entitySerialTime, String frequence, String fileName) {

		Hashtable<Integer, Hashtable<String, Float>> serialTimeResult;
		switch (frequence) {
		case "D":
			serialTimeResult = splitDay(dtInitial, dtEnd, entitySerialTime);
			break;
		case "M":
			serialTimeResult = splitMonth(dtInitial, dtEnd, entitySerialTime);
			break;
		case "W":
			serialTimeResult = splitWeek(dtInitial, dtEnd, entitySerialTime);
			break;
		case "C":
			serialTimeResult = splitCustom(dtInitial, dtEnd, entitySerialTime, fileName);
			break;
		default:
			serialTimeResult = new Hashtable<>();
			break;
		}

		Save save = new Save();
		save.setPath(Properties.getProperty("pathCalculationMetric"));
		save.setExtension("csv");
		save.summarizationMetrics(serialTimeResult, fileName);

		return serialTimeResult;
	}

	public boolean summarizationMetric() {

		// Carregar todas as entidades que sofreram atualização na serial_time
		JSONArray jarrEntities = new JSONArray();

		for (int i = 0; i < jarrEntities.size(); i++) {
			splitDay((JSONObject) jarrEntities.get(i));
			splitMonth((JSONObject) jarrEntities.get(i));
			splitWeek((JSONObject) jarrEntities.get(i));
			splitCustom((JSONObject) jarrEntities.get(i)); // Consulta a
															// collection PERIOD
															// no banco de dados
															// e realiza calculo
															// de metrica para
															// todos
		}

		return true;
	}

	private void splitDay(JSONObject entity) {

		/*
		 * Hashtable<Integer, Hashtable<String, Float>> serialTimeResult = new
		 * Hashtable<>(); List<EntitySentiment> listAux = null;
		 * 
		 * int order = 1; DateTime dtAux = dtInitial; while
		 * (dtAux.isBefore(dtEnd.getMillis()) ||
		 * dtAux.isEqual(dtEnd.getMillis())) { listAux = new
		 * ArrayList<EntitySentiment>();
		 * 
		 * if (entitySerialTime.get(dtAux.getMillis()) != null)
		 * listAux.add(entitySerialTime.get(dtAux.getMillis()));
		 * 
		 * Hashtable<String, Float> htAuxValues = serialTimeResult.get(order);
		 * if (htAuxValues == null) htAuxValues = new Hashtable<>();
		 * 
		 * if (listAux.size() > 0) { htAuxValues.put("s1",
		 * s1PositivePerNegativa(listAux)); htAuxValues.put("s2",
		 * s2PositivePorPostiveNegative(listAux)); htAuxValues.put("s3",
		 * s3NegativePorPositiveNegative(listAux)); htAuxValues.put("s4",
		 * s4PositiveMenusNegativePerPostivieNegative(listAux));
		 * 
		 * serialTimeResult.put(order++, htAuxValues); } else {
		 * htAuxValues.put("s1", (float) 0); htAuxValues.put("s2", (float) 0);
		 * htAuxValues.put("s3", (float) 0); htAuxValues.put("s4", (float) 0);
		 * 
		 * serialTimeResult.put(order++, htAuxValues); }
		 * 
		 * dtAux = dtAux.plusDays(1); }
		 */
	}

	private void splitWeek(JSONObject entity) {

		/*
		 * Hashtable<Integer, Hashtable<String, Float>> serialTimeResult = new
		 * Hashtable<>();
		 * 
		 * List<EntitySentiment> listAux = null; DateTime date = dtInitial; int
		 * week = dtInitial.getWeekyear(); int order = 1;
		 * 
		 * while (date.isBefore(dtEnd.getMillis()) ||
		 * date.isEqual(dtEnd.getMillis())) { listAux = new
		 * ArrayList<EntitySentiment>();
		 * 
		 * while (true) { if (entitySerialTime.get(date.getMillis()) != null)
		 * listAux.add(entitySerialTime.get(date.getMillis()));
		 * 
		 * date = date.plusDays(1);
		 * 
		 * if (week != date.getWeekyear() ||
		 * dtEnd.plusDays(1).isEqual(date.getMillis())) { week =
		 * date.getWeekyear(); break; } }
		 * 
		 * Hashtable<String, Float> htAuxValues = serialTimeResult.get(order);
		 * if (htAuxValues == null) htAuxValues = new Hashtable<>();
		 * 
		 * if (listAux.size() > 0) { htAuxValues.put("s1",
		 * s1PositivePerNegativa(listAux)); htAuxValues.put("s2",
		 * s2PositivePorPostiveNegative(listAux)); htAuxValues.put("s3",
		 * s3NegativePorPositiveNegative(listAux)); htAuxValues.put("s4",
		 * s4PositiveMenusNegativePerPostivieNegative(listAux));
		 * 
		 * serialTimeResult.put(order++, htAuxValues); } else {
		 * htAuxValues.put("s1", (float) 0); htAuxValues.put("s2", (float) 0);
		 * htAuxValues.put("s3", (float) 0); htAuxValues.put("s4", (float) 0);
		 * 
		 * serialTimeResult.put(order++, htAuxValues); } }
		 */
	}

	private void splitMonth(JSONObject entity) {

		/*
		 * Hashtable<Integer, Hashtable<String, Float>> serialTimeResult = new
		 * Hashtable<>(); List<EntitySentiment> listAux = null; DateTime date =
		 * dtInitial; int month = dtInitial.getMonthOfYear(); int order = 1;
		 * 
		 * while (date.isBefore(dtEnd.getMillis()) ||
		 * date.isEqual(dtEnd.getMillis())) { listAux = new
		 * ArrayList<EntitySentiment>();
		 * 
		 * while (true) { if (entitySerialTime.get(date.getMillis()) != null)
		 * listAux.add(entitySerialTime.get(date.getMillis()));
		 * 
		 * date = date.plusDays(1);
		 * 
		 * if (month != date.getMonthOfYear() ||
		 * dtEnd.plusDays(1).isEqual(date.getMillis())) { month =
		 * date.getMonthOfYear(); break; } }
		 * 
		 * Hashtable<String, Float> htAuxValues = serialTimeResult.get(order);
		 * if (htAuxValues == null) htAuxValues = new Hashtable<>();
		 * 
		 * if (listAux.size() > 0) { htAuxValues.put("s1",
		 * s1PositivePerNegativa(listAux)); htAuxValues.put("s2",
		 * s2PositivePorPostiveNegative(listAux)); htAuxValues.put("s3",
		 * s3NegativePorPositiveNegative(listAux)); htAuxValues.put("s4",
		 * s4PositiveMenusNegativePerPostivieNegative(listAux));
		 * 
		 * serialTimeResult.put(order++, htAuxValues); } else {
		 * htAuxValues.put("s1", (float) 0); htAuxValues.put("s2", (float) 0);
		 * htAuxValues.put("s3", (float) 0); htAuxValues.put("s4", (float) 0);
		 * 
		 * serialTimeResult.put(order++, htAuxValues); } }
		 */
	}

	private void splitCustom(JSONObject entity) {

		/*
		 * Hashtable<Integer, Hashtable<String, Float>> serialTimeResult = new
		 * Hashtable<>(); Load load = new Load(); List<EntitySentiment> listAux
		 * = new ArrayList<EntitySentiment>(); List<DateTime> listDates = load
		 * .getEndDateED(Properties.getProperty("fileExternalData") +
		 * File.separator + fileName + ".csv");
		 * 
		 * DateTime dtStart = dtInitial; DateTime dtLimit = dtEnd.minusDays(1);
		 * int order = 1;
		 * 
		 * while (dtLimit.isBefore(dtEnd) || dtLimit.isEqual(dtEnd)) { //
		 * listAux = new ArrayList<EntitySentiment>();
		 * 
		 * dtLimit = listDates.get(order - 1);
		 * 
		 * while (dtStart.isBefore(dtLimit) || dtStart.equals(dtLimit)) {
		 * 
		 * if (entitySerialTime.get(dtStart.getMillis()) != null)
		 * listAux.add(entitySerialTime.get(dtStart.getMillis()));
		 * 
		 * dtStart = dtStart.plusDays(1); }
		 * 
		 * Hashtable<String, Float> htAuxValues = serialTimeResult.get(order);
		 * if (htAuxValues == null) htAuxValues = new Hashtable<>();
		 * 
		 * if (listAux.size() > 0) { htAuxValues.put("s1",
		 * s1PositivePerNegativa(listAux)); htAuxValues.put("s2",
		 * s2PositivePorPostiveNegative(listAux)); htAuxValues.put("s3",
		 * s3NegativePorPositiveNegative(listAux)); htAuxValues.put("s4",
		 * s4PositiveMenusNegativePerPostivieNegative(listAux));
		 * 
		 * serialTimeResult.put(order++, htAuxValues); } else {
		 * htAuxValues.put("s1", (float) 0); htAuxValues.put("s2", (float) 0);
		 * htAuxValues.put("s3", (float) 0); htAuxValues.put("s4", (float) 0);
		 * 
		 * serialTimeResult.put(order++, htAuxValues); }
		 * 
		 * if (order > listDates.size()) break; }
		 */
	}

	private Hashtable<Integer, Hashtable<String, Float>> splitDay(DateTime dtInitial, DateTime dtEnd,
			Hashtable<Long, EntitySentiment> entitySerialTime) {

		Hashtable<Integer, Hashtable<String, Float>> serialTimeResult = new Hashtable<>();
		List<EntitySentiment> listAux = null;

		int order = 1;
		DateTime dtAux = dtInitial;
		while (dtAux.isBefore(dtEnd.getMillis()) || dtAux.isEqual(dtEnd.getMillis())) {
			listAux = new ArrayList<EntitySentiment>();

			if (entitySerialTime.get(dtAux.getMillis()) != null)
				listAux.add(entitySerialTime.get(dtAux.getMillis()));

			Hashtable<String, Float> htAuxValues = serialTimeResult.get(order);
			if (htAuxValues == null)
				htAuxValues = new Hashtable<>();

			if (listAux.size() > 0) {
				htAuxValues.put("s1", s1PositivePerNegativa(listAux));
				htAuxValues.put("s2", s2PositivePorPostiveNegative(listAux));
				htAuxValues.put("s3", s3NegativePorPositiveNegative(listAux));
				htAuxValues.put("s4", s4PositiveMenusNegativePerPostivieNegative(listAux));

				serialTimeResult.put(order++, htAuxValues);
			} else {
				htAuxValues.put("s1", (float) 0);
				htAuxValues.put("s2", (float) 0);
				htAuxValues.put("s3", (float) 0);
				htAuxValues.put("s4", (float) 0);

				serialTimeResult.put(order++, htAuxValues);
			}

			dtAux = dtAux.plusDays(1);
		}

		return serialTimeResult;
	}

	private Hashtable<Integer, Hashtable<String, Float>> splitWeek(DateTime dtInitial, DateTime dtEnd,
			Hashtable<Long, EntitySentiment> entitySerialTime) {

		Hashtable<Integer, Hashtable<String, Float>> serialTimeResult = new Hashtable<>();

		List<EntitySentiment> listAux = null;
		DateTime date = dtInitial;
		int week = dtInitial.getWeekyear();
		int order = 1;

		while (date.isBefore(dtEnd.getMillis()) || date.isEqual(dtEnd.getMillis())) {
			listAux = new ArrayList<EntitySentiment>();

			while (true) {
				if (entitySerialTime.get(date.getMillis()) != null)
					listAux.add(entitySerialTime.get(date.getMillis()));

				date = date.plusDays(1);

				if (week != date.getWeekyear() || dtEnd.plusDays(1).isEqual(date.getMillis())) {
					week = date.getWeekyear();
					break;
				}
			}

			Hashtable<String, Float> htAuxValues = serialTimeResult.get(order);
			if (htAuxValues == null)
				htAuxValues = new Hashtable<>();

			if (listAux.size() > 0) {
				htAuxValues.put("s1", s1PositivePerNegativa(listAux));
				htAuxValues.put("s2", s2PositivePorPostiveNegative(listAux));
				htAuxValues.put("s3", s3NegativePorPositiveNegative(listAux));
				htAuxValues.put("s4", s4PositiveMenusNegativePerPostivieNegative(listAux));

				serialTimeResult.put(order++, htAuxValues);
			} else {
				htAuxValues.put("s1", (float) 0);
				htAuxValues.put("s2", (float) 0);
				htAuxValues.put("s3", (float) 0);
				htAuxValues.put("s4", (float) 0);

				serialTimeResult.put(order++, htAuxValues);
			}
		}

		return serialTimeResult;
	}

	private Hashtable<Integer, Hashtable<String, Float>> splitMonth(DateTime dtInitial, DateTime dtEnd,
			Hashtable<Long, EntitySentiment> entitySerialTime) {

		Hashtable<Integer, Hashtable<String, Float>> serialTimeResult = new Hashtable<>();
		List<EntitySentiment> listAux = null;
		DateTime date = dtInitial;
		int month = dtInitial.getMonthOfYear();
		int order = 1;

		while (date.isBefore(dtEnd.getMillis()) || date.isEqual(dtEnd.getMillis())) {
			listAux = new ArrayList<EntitySentiment>();

			while (true) {
				if (entitySerialTime.get(date.getMillis()) != null)
					listAux.add(entitySerialTime.get(date.getMillis()));

				date = date.plusDays(1);

				if (month != date.getMonthOfYear() || dtEnd.plusDays(1).isEqual(date.getMillis())) {
					month = date.getMonthOfYear();
					break;
				}
			}

			Hashtable<String, Float> htAuxValues = serialTimeResult.get(order);
			if (htAuxValues == null)
				htAuxValues = new Hashtable<>();

			if (listAux.size() > 0) {
				htAuxValues.put("s1", s1PositivePerNegativa(listAux));
				htAuxValues.put("s2", s2PositivePorPostiveNegative(listAux));
				htAuxValues.put("s3", s3NegativePorPositiveNegative(listAux));
				htAuxValues.put("s4", s4PositiveMenusNegativePerPostivieNegative(listAux));

				serialTimeResult.put(order++, htAuxValues);
			} else {
				htAuxValues.put("s1", (float) 0);
				htAuxValues.put("s2", (float) 0);
				htAuxValues.put("s3", (float) 0);
				htAuxValues.put("s4", (float) 0);

				serialTimeResult.put(order++, htAuxValues);
			}
		}

		return serialTimeResult;
	}

	private Hashtable<Integer, Hashtable<String, Float>> splitCustom(DateTime dtInitial, DateTime dtEnd,
			Hashtable<Long, EntitySentiment> entitySerialTime, String fileName) {

		Hashtable<Integer, Hashtable<String, Float>> serialTimeResult = new Hashtable<>();
		Load load = new Load();
		List<EntitySentiment> listAux = new ArrayList<EntitySentiment>();
		List<DateTime> listDates = load
				.getEndDateED(Properties.getProperty("fileExternalData") + File.separator + fileName + ".csv");

		DateTime dtStart = dtInitial;
		DateTime dtLimit = dtEnd.minusDays(1);
		int order = 1;

		while (dtLimit.isBefore(dtEnd) || dtLimit.isEqual(dtEnd)) {
			// listAux = new ArrayList<EntitySentiment>();

			dtLimit = listDates.get(order - 1);

			while (dtStart.isBefore(dtLimit) || dtStart.equals(dtLimit)) {

				if (entitySerialTime.get(dtStart.getMillis()) != null)
					listAux.add(entitySerialTime.get(dtStart.getMillis()));

				dtStart = dtStart.plusDays(1);
			}

			Hashtable<String, Float> htAuxValues = serialTimeResult.get(order);
			if (htAuxValues == null)
				htAuxValues = new Hashtable<>();

			if (listAux.size() > 0) {
				htAuxValues.put("s1", s1PositivePerNegativa(listAux));
				htAuxValues.put("s2", s2PositivePorPostiveNegative(listAux));
				htAuxValues.put("s3", s3NegativePorPositiveNegative(listAux));
				htAuxValues.put("s4", s4PositiveMenusNegativePerPostivieNegative(listAux));

				serialTimeResult.put(order++, htAuxValues);
			} else {
				htAuxValues.put("s1", (float) 0);
				htAuxValues.put("s2", (float) 0);
				htAuxValues.put("s3", (float) 0);
				htAuxValues.put("s4", (float) 0);

				serialTimeResult.put(order++, htAuxValues);
			}

			if (order > listDates.size())
				break;
		}

		return serialTimeResult;
	}

	// Arquivo já vem com sequencial númerico e valores das fórmulas
	public void sComposite(Set<String> names, Hashtable<String, List<SumarySentiment>> htList) {
		// 0 = C, 1 = P, 2 = E
		double[] weight;
		Hashtable<String, double[][]> matrixValues = new Hashtable<>();

		List<SumarySentiment> lsCandidate, lsParty, lsRelation;

		// Repetição para o número de fórmulas
		for (int j = 1; j < 2; j++) {

			for (String name : names) {
				lsCandidate = htList.get(name + "-C");
				lsParty = htList.get(name + "-P");
				lsRelation = htList.get(name + "-E");

				double[][] mtValues = new double[lsCandidate.size()][3];

				for (int i = 0; i < lsCandidate.size(); i++) {
					mtValues[i][0] = lsCandidate.get(i).getValueIndex(j);
					mtValues[i][1] = lsParty.get(i).getValueIndex(j);
					mtValues[i][2] = lsRelation.get(i).getValueIndex(j);
				}

				matrixValues.put(name, mtValues);
			}

			// Definir pesos
			CalculateWeight cw = new CalculateWeight(names, matrixValues);
			weight = cw.calculateWeigth();

			// Calcular e salvar
			for (String name : names) {
				double[][] mtValues = matrixValues.get(name);
				double[] arrValue = new double[mtValues.length];

				for (int i = 0; i < mtValues.length; i++) {
					arrValue[i] = mtValues[i][0] * weight[0] + mtValues[i][1] * weight[1] + mtValues[i][2] * weight[2];
				}

				// Salvar o arrValue
			}
		}
	}

	public float s1PositivePerNegativa(List<EntitySentiment> list) {
		// Total de positiva dividido por total de negativo, por candidato
		float totalPositiveDM = 0;
		float totalNegativeDM = 0;

		for (EntitySentiment sentiment : list) {
			totalPositiveDM += sentiment.getPositiveSentimentDM();
			totalNegativeDM += sentiment.getNegativaSentimentDM();
		}

		return totalPositiveDM / totalNegativeDM;
	}

	public float s2PositivePorPostiveNegative(List<EntitySentiment> list) {
		// Total de positivo dividido por total de positivo + negativo, por
		// candidato
		float totalPositiveDM = 0;
		float totalNegativeDM = 0;

		for (EntitySentiment sentiment : list) {
			totalPositiveDM += sentiment.getPositiveSentimentDM();
			totalNegativeDM += sentiment.getNegativaSentimentDM();
		}

		return totalPositiveDM / (totalPositiveDM + totalNegativeDM);
	}

	public float s3NegativePorPositiveNegative(List<EntitySentiment> list) {
		// Total de negativo dividido por total de positivo + negativo, por
		// candidato
		float totalPositiveDM = 0;
		float totalNegativeDM = 0;

		for (EntitySentiment sentiment : list) {
			totalPositiveDM += sentiment.getPositiveSentimentDM();
			totalNegativeDM += sentiment.getNegativaSentimentDM();
		}

		return totalNegativeDM / (totalPositiveDM + totalNegativeDM);
	}

	public float s4PositiveMenusNegativePerPostivieNegative(List<EntitySentiment> list) {
		// Total de positivo menos negativo dividido por total de positivo +
		// negativo, por candidato
		float totalPositiveDM = 0;
		float totalNegativeDM = 0;

		for (EntitySentiment sentiment : list) {
			totalPositiveDM += sentiment.getPositiveSentimentDM();
			totalNegativeDM += sentiment.getNegativaSentimentDM();
		}

		return (totalPositiveDM - totalNegativeDM) / (totalPositiveDM + totalNegativeDM);
	}

	public float s5PositiveOneEntityPorTotalPositivesAllEntities(List<EntitySentiment> list, String name) {
		// Total de positivo de um candidato dividido pelo total de positivo de
		// todos os candidatos
		float totalPositiveUnitDM = 0;
		float totalPositiveAllDM = 0;

		for (EntitySentiment sentiment : list) {
			if (sentiment.getEntityName().equals(name))
				totalPositiveUnitDM += sentiment.getPositiveSentimentDM();

			totalPositiveAllDM += sentiment.getPositiveSentimentDM();
		}

		return totalPositiveUnitDM / totalPositiveAllDM;
	}

	public float s6NegativeOneEntityPerTotalNegativeAllEntities(List<EntitySentiment> list, String name) {
		// Total de negativo de um candidato dividido pelo total de negativo de
		// todos os candidatos
		float totalNegativeUnitDM = 0;
		float totalNegativeAllDM = 0;

		for (EntitySentiment sentiment : list) {
			if (sentiment.getEntityName().equals(name))
				totalNegativeUnitDM += sentiment.getNegativaSentimentDM();

			totalNegativeAllDM += sentiment.getNegativaSentimentDM();
		}

		return totalNegativeUnitDM / totalNegativeAllDM;
	}

	public float s7NumberMentionsOneEntityPerNumberMentionsAllEntities(List<EntitySentiment> list, String name) {
		// Número de menções a um candidato dividido pelo numero de menções a
		// todos os candidatos
		int totalNumberMentionsUnit = 0;
		int totalNumberMentionsAll = 0;

		for (EntitySentiment sentiment : list) {
			if (sentiment.getEntityName().equals(name))
				totalNumberMentionsUnit += sentiment.getNumberDirectMentions() + sentiment.getNumberCoMentions();

			totalNumberMentionsAll += sentiment.getNumberDirectMentions() + sentiment.getNumberCoMentions();
		}

		return totalNumberMentionsUnit / totalNumberMentionsAll;
	}

	public double calculateCorrelation(double[] a, double[] b) {
		// com a série gerada e com uma série informada calcular a correlação
		// dos dados
		return new PearsonsCorrelation().correlation(a, b);
	}

	public void parse(JSONObject entitiesSentiments) {
		List<EntitySentiment> lsEntSent = new ArrayList<>();
		System.out.println("File: " + entitiesSentiments.get("fileName").toString());
		Long date = Dates.dateTime(entitiesSentiments.get("date").toString()).getMillis();
		Integer lengthText = Integer.parseInt(entitiesSentiments.get("lengthText").toString());

		if (numbersDocs.containsKey(date)) {
			numbersDocs.put(date, numbersDocs.get(date) + 1);
		} else {
			numbersDocs.put(date, 1);
		}

		if (lengthTexts.containsKey(date)) {
			lengthTexts.put(date, lengthTexts.get(date) + lengthText);
		} else {
			lengthTexts.put(date, lengthText);
		}

		JSONArray arEntities = (JSONArray) entitiesSentiments.get("entities");

		for (int i = 0; arEntities != null && i < arEntities.size(); i++) {
			String name = ((JSONObject) arEntities.get(i)).get("name").toString();

			JSONArray arMentions = (JSONArray) ((JSONObject) arEntities.get(i)).get("mentions");

			int nCoMentions = 0;
			int nDiMentions = 0;
			float positiveSentDM = 0;
			float negativeSentDM = 0;
			float positiveSentCM = 0;
			float negativeSentCM = 0;

			for (int j = 0; j < arMentions.size(); j++) {
				if (((JSONObject) arMentions.get(j)).get("type").toString().equals("PROPER")) {
					nDiMentions++;

					float sentAux = Float.parseFloat(((JSONObject) arMentions.get(j)).get("score").toString());
					if (sentAux > 0) {
						positiveSentDM += sentAux;
					} else {
						negativeSentDM += sentAux;
					}
				} else {
					nCoMentions++;

					float sentAux = Float.parseFloat(((JSONObject) arMentions.get(j)).get("score").toString());
					if (sentAux > 0) {
						positiveSentCM += sentAux;
					} else {
						negativeSentCM += sentAux;
					}
				}

			}

			if (nDiMentions != 0) {
				EntitySentiment esAux = new EntitySentiment();

				esAux.setEntityName(name);
				esAux.setNumberCoMentions(nCoMentions);
				esAux.setNumberDirectMentions(nDiMentions);
				esAux.setPositiveSentimentDM(positiveSentDM);
				esAux.setNegativaSentimentDM(negativeSentDM);
				esAux.setPositiveSentimentCM(positiveSentCM);
				esAux.setNegativaSentimentCM(negativeSentCM);

				lsEntSent.add(esAux);
			}

		}

		if (this.entitiesTime.containsKey(date)) {
			List<EntitySentiment> lsAux = this.entitiesTime.get(date);
			lsAux.addAll(lsEntSent);
			this.entitiesTime.put(date, lsAux);
		} else {
			this.entitiesTime.put(date, lsEntSent);
		}
	}

	public void generationSerialTime() throws UnknownHostException, ParseException {
		LoadDocuments ld = new LoadDocuments(host, databaseName, collection);
		SaveDocuments sd = new SaveDocuments(host, databaseName, collection);

		// Recupera todas as mentions com algum status
		JSONArray jarr = ld.findByQuery(new BasicDBObject().append("is_serialtime", "false"), 1);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		for (int i = 0; i < jarr.size(); i++) {
			// Pega os documentos e começa agrupar data por data
			JSONArray ltDocuments = (JSONArray) ((JSONObject) jarr.get(i)).get("documents");

			Hashtable<Date, BasicDBObject> htSerialTime = new Hashtable<>();
			Date dateMin = null;
			Date dateMax = null;
			for (int j = 0; j < ltDocuments.size(); j++) {
				Date date = formatter.parse(((JSONObject) ltDocuments.get(j)).get("date").toString());

				if (dateMin == null && dateMax == null)
					dateMin = dateMax = date;
				else if (date.before(dateMin))
					dateMin = date;
				else if (date.after(dateMax))
					dateMax = date;

				double score_direct_pos = Double
						.parseDouble(((JSONObject) ltDocuments.get(j)).get("score_direct_pos").toString());
				double score_direct_neg = Double
						.parseDouble(((JSONObject) ltDocuments.get(j)).get("score_direct_neg").toString());
				double score_coref_pos = Double
						.parseDouble(((JSONObject) ltDocuments.get(j)).get("score_coref_pos").toString());
				double score_coref_neg = Double
						.parseDouble(((JSONObject) ltDocuments.get(j)).get("score_coref_neg").toString());

				if (htSerialTime.containsKey(date)) {
					BasicDBObject basic = htSerialTime.get(date);
					basic.replace("score_direct_pos", basic.getDouble("score_direct_pos") + score_direct_pos);
					basic.replace("score_direct_neg", basic.getDouble("score_direct_neg") + score_direct_neg);
					basic.replace("score_coref_pos", basic.getDouble("score_coref_pos") + score_coref_pos);
					basic.replace("score_coref_neg", basic.getDouble("score_coref_neg") + score_coref_neg);

					htSerialTime.replace(date, basic);
				} else {
					BasicDBObject basic = new BasicDBObject();
					basic.append("score_direct_pos", score_direct_pos).append("score_direct_neg", score_direct_neg)
							.append("score_coref_pos", score_coref_pos).append("score_coref_neg", score_coref_neg);

					htSerialTime.put(date, basic);
				}
			}

			Date date = dateMin;
			BasicDBList ltSerialTimeS = new BasicDBList();
			BasicDBList ltSerialTimeA = new BasicDBList();
			double score_direct_pos = 0;
			double score_direct_neg = 0;
			double score_coref_pos = 0;
			double score_coref_neg = 0;

			// duas series são feitas SerialTimeShort e SerialTimeAcum
			// Valores do atributo são date (timestamp) e sentiments
			// (score_direct_pos, score_direct_neg, score_coref_pos,
			// score_coref_neg)
			while (date.before(dateMax) || date.equals(dateMax)) {
				if (htSerialTime.containsKey(date)) {
					ltSerialTimeS
							.add(new BasicDBObject().append("date", date).append("sentiments", htSerialTime.get(date)));
					
					BasicDBObject sent = htSerialTime.get(date);
					score_direct_pos += sent.getDouble("score_direct_pos");
					score_direct_neg += sent.getDouble("score_direct_neg");
					score_coref_pos += sent.getDouble("score_coref_pos");
					score_coref_neg += sent.getDouble("score_coref_neg");
				} else {
					ltSerialTimeS.add(new BasicDBObject().append("date", date).append("sentiments", null));
				}

				ltSerialTimeA.add(new BasicDBObject().append("date", date).append("sentiments",
						new BasicDBObject().append("score_direct_pos", score_direct_pos)
								.append("score_direct_neg", score_direct_neg).append("score_coref_pos", score_coref_pos)
								.append("score_coref_neg", score_coref_neg)));

				LocalDateTime.from(date.toInstant()).plusDays(1);
			}
			
			// salva com as datas ordenadas
			sd.updateDocument(new BasicDBObject().append("$set", new BasicDBObject().append("is_serialtime", "true")
					.append("serial_time_short", ltSerialTimeS)
					.append("serial_time_acum", ltSerialTimeA)),
					new BasicDBObject().append("_id", ((JSONObject) jarr.get(i)).get("_id").toString()));
			
		}

	}
}
