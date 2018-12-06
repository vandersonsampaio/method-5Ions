package core.correlation;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.mongodb.BasicDBList;

import core.entity.ExternalData;
import io.file.Load;
import util.commom.Properties;

public class Correlation {

	private String host;
	private String databaseName;
	private String collection;
	
	public Correlation(){
		//this.coPearson = new double[6];
	}
	
	public Correlation(String host, String databaseName, String collection){
		this.host = host;
		this.databaseName = databaseName;
		this.collection = collection;
	}
	
	public void calculeCorrelation(){
		//Definir as entidades para análise
		//Colocar manualmente
		
		//Pegar as metrics calculadas
		BasicDBList entitiesAnalysis = new BasicDBList();
		
		//Pegar a medida externa para correlação
		Hashtable<Date, Double> htExterno = new Hashtable<>();
		
		//Jogar tudo no algoritmo genético
		CalculateWeight cw = new CalculateWeight(100, .6);
		cw.calculateWeigth(entitiesAnalysis, htExterno);
	}
	
	public double generationCorrelation(Set<String> names, Hashtable<String, double[]> datas){
		List<String> namesSorted = names.stream().collect(Collectors.toList());
		Collections.sort(namesSorted, (o1, o2) -> o1.compareTo(o2));
		
		Load load = new Load();
		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
		
		for(String name : namesSorted){
			double[] data = datas.get(name);
			double[] inExt = new double[data.length];
			
			List<ExternalData> listExtData = load.getExternalData(Properties.getProperty("fileExternalData") + File.separator + name + ".csv");
			
			for(int i = 0; i < listExtData.size(); i++){
				inExt[i] = listExtData.get(i).getValue();	
			}
			
			return Math.abs(pearsonsCorrelation.correlation(data, inExt));
			
		}
		
		return 0;
	}
	
}
