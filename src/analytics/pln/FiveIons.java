package analytics.pln;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import core.correlation.SerialTime;
import core.entity.EntitySentiment;
import core.prediction.AdapterWeka;
import core.semantic.annotation.googlecloud.EntityAnnotation;
import core.semantic.annotation.googlecloud.SentimentEntityAnnotation;
import core.semantic.sentimentanalysis.googlecloud.SentimentAnalysis;
import core.summarization.SumyPython;
import io.file.Load;
import io.file.Save;
import util.commom.Dates;
import util.commom.Files;
import util.commom.Properties;

public class FiveIons {

	private static Save save = new Save();
	private static EntityAnnotation entityAnnotation = new EntityAnnotation();
	private static SentimentAnalysis sentimentAnalysis = new SentimentAnalysis();
	private static SentimentEntityAnnotation sentimentEntityAnnotation = new SentimentEntityAnnotation();
	private static Load load = new Load();
	private static SerialTime serialTime = new SerialTime();
	private static SumyPython sumy = new SumyPython();
	private static AdapterWeka prediction = new AdapterWeka();
	
	public static void main(String[] args) {
		
		//boolean summarization = false;
		boolean annotationEntity = false;
		boolean annotationSentiment = false;
		boolean annotationEntSent = false;
		boolean genneration7uplas = false;
		boolean calculationMetrics = false;
		boolean correlation = false;
		boolean prediction = false;
		
		for(int i = 0; i < args.length; i++){
		
			//if(args[i].equals("sum"))
			//	summarization = true;
			
			if(args[i].equals("ent"))
				annotationEntity = true;
			
			if(args[i].equals("sen"))
				annotationSentiment = true;
			
			if(args[i].equals("ense"))
				annotationEntSent = true;
			
			if(args[i].equals("gen"))
				genneration7uplas = true;
			
			if(args[i].equals("calc"))
				calculationMetrics = true;
			
			if(args[i].equals("corr"))
				correlation = true;
			
			if(args[i].equals("pred"))
				prediction = true;
		}
		
		if(annotationEntity || annotationSentiment || annotationEntSent) {
			String fileName = "";
			
			try {
				//Carrega todos os documentos para iniciar a análise
				JSONArray documents = load.getDocuments();
				
				for(int i = 0; i < documents.size(); i++) {
					JSONObject document = (JSONObject) documents.get(i);
					
					fileName = document.get("fileName").toString().split("\\.")[0];
					String tittle = document.get("tittle").toString();
					//System.out.println(document.get("date").toString());
					String date = Dates.formatDateTime(document.get("date").toString());
					
					//System.out.println(fileName);
					String text = document.get("text").toString();
	
					save.setFileName(fileName);
					
					//Sumarizar documento por documento
					//if(summarization)
					//	summarizationText(fileName, tittle, date, text);
					
					//Anota as entidades
					if(annotationEntity)
						annotationEntity(fileName, tittle, date, text);
					
					//Anota os sentimentos
					if(annotationSentiment)
						sentimentAnalysis(fileName, tittle, date, text);
					
					//Anota os sentimentos das entidades
					if(annotationEntSent)
						entitySentiment(fileName, tittle, date, text);
				}
			} catch (Exception e) {
				System.out.println(fileName);
				e.printStackTrace();
			}
		}
		
		//Carrega o sentimento das entidades para gerar as 7-uplas
		if(genneration7uplas)
			generation7uplas("25/12/2016", "25/09/2017");
		
		//Carrega as 5-uplas para sumarizar as medidas de acordo com a granularidade desejada (Diário, Semanal, Mensal, Customizada)
		//Short-Time e Acumulative (Falta o acumulative)
		if(calculationMetrics)
			summarizationMetric("25/12/2016", "25/09/2017");
		
		//Correlação das duas séries temporais
		if(correlation)
			correlationSeries();
		
		//Gera arquivo ARFF do Weka para correlação
		if(prediction)
			prediction();
	}
	
	public static void summarizationText(String fileName, String tittle, String date, String text) {
		System.out.println("Start Summarization");
		
		if(!util.commom.Files.existsFile("C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\DMAlemanhaSumm\\", fileName, "summ")) {
			save.setExtension("summ");
			save.setFileName(fileName);
			save.setPath("C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\DMAlemanhaSumm");
			JSONObject summary = sumy.summarizationLuhn(tittle + "\r\n" + text, date);
			save.save(summary);
			System.out.println(fileName + ".summ ADICIONADO!");
		}
		
		System.out.println("End Summarization");
	}
	
	public static void prediction() {
		System.out.println("Start Prediction");
		
		prediction.gennerationARFF(2);
		
		System.out.println("End Prediction");
	}
	
	public static void correlationSeries() {
		System.out.println("Start Correlation Serial Time");
		List<String> listNames = Files.getAllFileNames(Properties.getProperty("pathCorrelation"));
		
		for(String fileName : listNames){
			double[] serieInternal = load.getSerialTimeCompact(Properties.getProperty("pathCorrelation"), fileName, 1);
			double[] serieCurrent = load.getSerialTimeCompact(Properties.getProperty("fileExternalData"), fileName, 4);
			
			double correlation = serialTime.calculateCorrelation(serieInternal, serieCurrent);

			System.out.println("Entidade: " + fileName + " Valor Correlacionado: " + correlation);
		}
		
		System.out.println("End Correlation Serial Time");
	}
	
	public static void summarizationMetric(String InitialDate, String EndDate) {
		System.out.println("Start Calculate Metrics");
		
		List<String> listFiles = Files.getAllFileNames(Properties.getProperty("pathSerialTimeMetric"));
		
		for(String fileName : listFiles) {
			Hashtable<Long, EntitySentiment> analise = load.getSerialTime(Properties.getProperty("pathSerialTimeMetric") + File.separator + fileName);
		
			serialTime.summarizationMetric(Dates.dateTime(InitialDate), Dates.dateTime(EndDate), analise, "C", fileName.split("\\.")[0]);
		}
		
		System.out.println("End Calculate Metrics");
	}
	
	public static void generation7uplas(String InitialDate, String EndDate) {
		System.out.println("Start Generation 7uplas");
		
		JSONArray entitiesSentiments = load.getEntitiesSentiment();
		for(int i = 0; i < entitiesSentiments.size(); i++) {
			serialTime.parse((JSONObject) entitiesSentiments.get(i));
		}
		
		serialTime.generationSerie(Dates.dateTime(InitialDate), Dates.dateTime(EndDate));
		
		System.out.println("End Generation 7uplas");
	}

	public static void annotationEntity(String fileName, String tittle, String date, String text) throws Exception {
	//	System.out.println("Start Annotation Entities");
		
		if(!util.commom.Files.existsFile("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Entidades\\DW_Alemao_Trans", fileName, "ents")) {
			save.setExtension("ents");
			save.setPath("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Entidades\\DW_Alemao_Trans");
			
			text = StringEscapeUtils.unescapeHtml4(text);
			
			long time_ini = System.currentTimeMillis();
			JSONObject annotation = entityAnnotation.analyzeEntitiesText(text, tittle, date);
			long time_end = System.currentTimeMillis();
			
			//Salvar o arquivo num CSV
			System.out.println(fileName + ";" + (time_end - time_ini));
			
			save.save(annotation);
//			System.out.println(fileName + ".ents ADICIONADO!");
		}
		
	//	System.out.println("End Annotation Entities");
	}
	
	public static void sentimentAnalysis(String fileName, String tittle, String date, String text) throws Exception {
		//System.out.println("Start Sentiment Analysis");
		
		if(!util.commom.Files.existsFile("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Sentimentos\\DW_Alemao_Trans", fileName, "sents")) {
			save.setExtension("sents");
			save.setPath("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Sentimentos\\DW_Alemao_Trans");
			
			text = StringEscapeUtils.unescapeHtml4(text);
			
			long time_ini = System.currentTimeMillis();
			JSONObject sentiment = sentimentAnalysis.analyzeSentimentText(text, tittle, date);
			long time_end = System.currentTimeMillis();
			
			//Salvar o arquivo num CSV
			System.out.println(fileName + ";" + (time_end - time_ini));
			
			save.save(sentiment);
			//System.out.println(fileName + ".sents ADICIONADO!");
		}
		
		//System.out.println("End Sentiment Analysis");
	}
	
	public static void entitySentiment(String fileName, String tittle, String date, String text) throws Exception {
		//System.out.println("Start Sentiment Entities");
		
		if(!util.commom.Files.existsFile("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\EntidadeSentimentos\\DW_Alemao_Trans", fileName, "entsents")) {
			save.setExtension("entsents");
			save.setPath("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\EntidadeSentimentos\\DW_Alemao_Trans");
			
			text = StringEscapeUtils.unescapeHtml4(text);
			
			long time_ini = System.currentTimeMillis();
			JSONObject sentEntity = sentimentEntityAnnotation.entitySentimentText(text, tittle, date);
			long time_end = System.currentTimeMillis();
			
			//Salvar o arquivo num CSV
			System.out.println(fileName + ";" + (time_end - time_ini));
			
			save.save(sentEntity);
			//System.out.println(fileName + ".entsents ADICIONADO!");
		}
		
		//System.out.println("End Sentiment Entities");
	}
}
