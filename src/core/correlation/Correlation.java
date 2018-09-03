package core.correlation;

import java.io.File;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import core.entity.ExternalData;
import io.file.Load;
import util.commom.Properties;

public class Correlation {

	//private double[] coPearson;
	//Métrica 1 - Emnid
	//private double[] coPearsonExt = {0.373195649, 0.560869655, 0.798327829, 0.698941145, 0.458933947, 0.74559834};
	
	//Métrica 2 - Emnid
	//private double[] coPearsonExt = {0.24216838, 0.58243293, 0.797960144, 0.677155991, 0.462570567, 0.670177317};
	
	public Correlation(){
		//this.coPearson = new double[6];
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
	
	/*public int numberValid(){
		int ret = 0;
		
		for(int i = 0; i < coPearson.length; i++){
			if(coPearson[i] >= coPearsonExt[i])
				ret++;
		}
		
		return ret;
	}*/
}
