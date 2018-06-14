package core.prediction;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import core.entity.ExternalData;
import io.file.Load;
import io.file.Save;
import util.commom.Files;
import util.commom.Properties;

public class AdapterWeka {

	private Load load;
	
	public AdapterWeka(){
		this.load = new Load();
		
	}
	
	public String loadDatas(int formula){
		Hashtable<Integer, Float> annotationReal = new Hashtable<>();
		Hashtable<Integer, Float> annotationSentiment = new Hashtable<>();
		List<String> listNames = Files.getAllFileNames(Properties.getProperty("pathCorrelation"));
		
		StringBuilder data = new StringBuilder();
		data.append("@DATA \r\n");

		for(String fileName : listNames){
			annotationReal.clear();
			annotationSentiment.clear();
			
			List<ExternalData> listExtData = load.getExternalData(Properties.getProperty("fileExternalData") + File.separator + fileName);
			double[] serieInternal = load.getSerialTimeCompact(Properties.getProperty("pathCorrelation"), fileName, formula);
			
			for(int i = 0; i < listExtData.size(); i++){
				annotationReal.put(i, listExtData.get(i).getValue());	
			}
			
			for(int i = 0; i < serieInternal.length; i++)
				annotationSentiment.put(i, (float) serieInternal[i]);
			
			data.append(this.gennerationData(annotationSentiment, annotationReal));
		}
		
		return data.toString();
	}
	
	public void gennerationARFF(int formula){
		String document = this.gennerationHead() + "\n\n" + this.gennerationAttribute() + "\n\n" + this.loadDatas(formula);
		
		Save save = new Save();
		save.setExtension("arff");
		save.setPath(Properties.getProperty("pathFolderWeka"));
		save.save(document, "dataSet_Metric" + formula + "_" + System.currentTimeMillis());
	}
	
	private String gennerationHead(){
		return "@RELATION " + Properties.getProperty("relationPrediction");
	}
	
	private String gennerationAttribute(){
		return "@attribute ini 	REAL\r\n" + 
				"@attribute end	REAL\r\n" + 
				"@attribute class		{Aumenta, Diminui, Mantem}\r\n";
	}
	
	private String gennerationData(Hashtable<Integer, Float> annotationSentiment, Hashtable<Integer, Float> annotationReal){
		StringBuilder data = new StringBuilder();

		Float diffValue;
		
		for(int i = 0; i < annotationReal.size() - 1; i++){
			diffValue = annotationReal.get(i + 1) - annotationReal.get(i);
			
			data.append(annotationSentiment.get(i));
			data.append(",");
			data.append(annotationSentiment.get(i + 1));
			data.append(",");
			data.append(diffValue > 0 ? "Aumenta" : diffValue < 0 ? "Diminui" : "Mantem");
			data.append("\n");
		}
		
		return data.toString();
	}
}
