package analytics.pln;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import core.correlation.SerialTime;
import core.entity.EntitySentiment;
import core.entity.SumarySentiment;
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

	private static final String HOST = "localhost";
	private static final String DATABASENAME = "db_news_brazil";
	private static Save save = new Save();
	private static EntityAnnotation entityAnnotation = new EntityAnnotation(HOST, DATABASENAME, "mentions",
			"documents");
	private static SentimentAnalysis sentimentAnalysis = new SentimentAnalysis(HOST, DATABASENAME, "documents");
	private static SentimentEntityAnnotation sentimentEntityAnnotation = new SentimentEntityAnnotation(HOST,
			DATABASENAME, "mentions", "documents");
	private static Load load = new Load();
	private static SerialTime serialTime = new SerialTime(HOST, DATABASENAME, "mentions");
	private static SumyPython sumy = new SumyPython();
	private static AdapterWeka prediction = new AdapterWeka();

	public static void main(String[] args) throws InterruptedException {

		boolean correlation = false;
		boolean prediction = false;

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("corr"))
				correlation = true;

			if (args[i].equals("pred"))
				prediction = true;
		}

		try {
			// Anota as entidades
			if(false) {
			entityAnnotation.analyzeEntitiesText();

			// Anota os sentimentos
			sentimentAnalysis.analyzeSentimentText();
			}

			// Anota os sentimentos das entidades
			sentimentEntityAnnotation.entitySentimentText();

			System.exit(0);
			// processo deverá ser integrado a análise de sentimentos
			// Carrega o sentimento das entidades para gerar as 7-uplas
			// if (genneration7uplas)
			// generation7uplas("25/12/2016", "25/09/2017");

			// Carrega as 5-uplas para sumarizar as medidas de acordo com a
			// granularidade
			// desejada (Diário, Semanal, Mensal, Customizada)
			// Short-Time e Acumulative (Falta o acumulative)
			// Não será mais passado parâmetros nesse método, o processo será em
			// todo o tempo das publicações para todas as métricas implementadas
			// if (calculationMetrics)
			// summarizationMetric("25/12/2016", "25/09/2017");
			// serialTime.summarizationMetric();
			serialTime.generationSerialTime();
			serialTime.summarizationMetric();

		} catch (UnknownHostException | ParseException | java.text.ParseException | ClassNotFoundException
				| InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		// Correlação das duas séries temporais
		// Série externa deverá ser informada aqui
		//
		if (correlation)
			correlationSeries();

		// Adicionar contribuição
		// rever isso tudo. Ajuste de pesos será interno a saída.
		if (true)
			compositeMetric();

		// Gera arquivo ARFF do Weka para correlação
		if (prediction)
			prediction();
	}

	public static void compositeMetric() {
		Set<String> names = new HashSet<>();

		names.add("AfD");
		// names.add("CDU");
		// names.add("FDP");
		// names.add("GRUNE");
		// names.add("LINKE");
		// names.add("SPD");

		String pathC = "C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Research\\Metric LSA\\Fors\\C\\";
		String pathP = "C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Research\\Metric LSA\\Fors\\P\\";
		String pathE = "C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Research\\Metric LSA\\Fors\\E\\";

		Hashtable<String, List<SumarySentiment>> list = new Hashtable<>();

		list.put("AfD-C", load.getSerialTimeCompact(pathC, "AfD.csv"));
		list.put("AfD-P", load.getSerialTimeCompact(pathP, "AfD.csv"));
		list.put("AfD-E", load.getSerialTimeCompact(pathE, "AfD.csv"));

		// list.put("CDU-C", load.getSerialTimeCompact(pathC, "CDU.csv"));
		// list.put("CDU-P", load.getSerialTimeCompact(pathP, "CDU.csv"));
		// list.put("CDU-E", load.getSerialTimeCompact(pathE, "CDU.csv"));

		// list.put("FDP-C", load.getSerialTimeCompact(pathC, "FDP.csv"));
		// list.put("FDP-P", load.getSerialTimeCompact(pathP, "FDP.csv"));
		// list.put("FDP-E", load.getSerialTimeCompact(pathE, "FDP.csv"));

		// list.put("GRUNE-C", load.getSerialTimeCompact(pathC, "GRUNE.csv"));
		// list.put("GRUNE-P", load.getSerialTimeCompact(pathP, "GRUNE.csv"));
		// list.put("GRUNE-E", load.getSerialTimeCompact(pathE, "GRUNE.csv"));

		// list.put("LINKE-C", load.getSerialTimeCompact(pathC, "LINKE.csv"));
		// list.put("LINKE-P", load.getSerialTimeCompact(pathP, "LINKE.csv"));
		// list.put("LINKE-E", load.getSerialTimeCompact(pathE, "LINKE.csv"));

		// list.put("SPD-C", load.getSerialTimeCompact(pathC, "SPD.csv"));
		// list.put("SPD-P", load.getSerialTimeCompact(pathP, "SPD.csv"));
		// list.put("SPD-E", load.getSerialTimeCompact(pathE, "SPD.csv"));

		serialTime.sComposite(names, list);
	}

	public static void summarizationText(String fileName, String tittle, String date, String text) {
		System.out.println("Start Summarization");

		if (!util.commom.Files.existsFile(
				"C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\DMAlemanhaSumm\\", fileName, "summ")) {
			save.setExtension("summ");
			save.setFileName(fileName);
			save.setPath("C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\DMAlemanhaSumm");
			JSONObject summary = sumy.summarizationLuhn(tittle + "\r\n" + text, date);
			save.save(summary);
			System.out.println(fileName + ".summ ADICIONADO!");
		}

		System.out.println("End Summarization");
	}

	public static void prediction() throws InterruptedException {
		System.out.println("Start Prediction");

		prediction.gennerationARFF();

		System.out.println("End Prediction");
	}

	public static void correlationSeries() {
		System.out.println("Start Correlation Serial Time");
		List<String> listNames = Files.getAllFileNames(Properties.getProperty("pathCorrelation"));

		for (String fileName : listNames) {
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

		for (String fileName : listFiles) {
			if (fileName.contains(".ini"))
				continue;

			Hashtable<Long, EntitySentiment> analise = load
					.getSerialTime(Properties.getProperty("pathSerialTimeMetric") + File.separator + fileName);

			serialTime.summarizationMetric(Dates.dateTime(InitialDate), Dates.dateTime(EndDate), analise, "C",
					fileName.split("\\.")[0]);
		}

		System.out.println("End Calculate Metrics");
	}

	public static void generation7uplas(String InitialDate, String EndDate) {
		System.out.println("Start Generation 7uplas");

		JSONArray entitiesSentiments = load.getEntitiesSentiment();
		for (int i = 0; i < entitiesSentiments.size(); i++) {
			serialTime.parse((JSONObject) entitiesSentiments.get(i));
		}

		serialTime.generationSerie(Dates.dateTime(InitialDate), Dates.dateTime(EndDate));

		System.out.println("End Generation 7uplas");
	}
}
