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
	
	public String loadDatas(int formula, int classes){
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
			
			if(classes == 0)
				data.append(this.gennerationData3Classes(annotationSentiment, annotationReal));
			else if(classes == 1)
				data.append(this.gennerationData2ClassesChange(annotationSentiment, annotationReal));
			else
				data.append(this.gennerationData2ClassesUpDown(annotationSentiment, annotationReal));
		}
		
		return data.toString();
	}
	
	public void gennerationARFF(){
		Save save = new Save();
		save.setExtension("arff");
		save.setPath(Properties.getProperty("pathFolderWeka"));
		
		String document = this.gennerationHead() + "\n\n" + this.gennerationAttribute("Aumenta, Diminui, Mantem") + "\n\n" + this.loadDatas(1, 0);
		save.save(document, "Emnid_Metric1_3Classes");
		
		document = this.gennerationHead() + "\n\n" + this.gennerationAttribute("Aumenta, Diminui, Mantem") + "\n\n" + this.loadDatas(2, 0);
		save.save(document, "Emnid_Metric2_3Classes");
		
		document = this.gennerationHead() + "\n\n" + this.gennerationAttribute("Muda, Mantem") + "\n\n" + this.loadDatas(1, 1);
		save.save(document, "Emnid_Metric1_2Classes_MM");
		
		document = this.gennerationHead() + "\n\n" + this.gennerationAttribute("Muda, Mantem") + "\n\n" + this.loadDatas(2, 1);
		save.save(document, "Emnid_Metric2_2Classes_MM");
		
		document = this.gennerationHead() + "\n\n" + this.gennerationAttribute("Aumenta, Diminui") + "\n\n" + this.loadDatas(1, 2);
		save.save(document, "Emnid_Metric1_2Classes_AD");
		
		document = this.gennerationHead() + "\n\n" + this.gennerationAttribute("Aumenta, Diminui") + "\n\n" + this.loadDatas(2, 2);
		save.save(document, "Emnid_Metric2_2Classes_AD");
		
		try {
			new WekaClassifier().calculeAcurracy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private String gennerationHead(){
		return "@RELATION " + Properties.getProperty("relationPrediction");
	}
	
	private String gennerationAttribute(String classes){
		return "@attribute ini 	REAL\r\n" + 
				"@attribute end	REAL\r\n" + 
				"@attribute class		{" + classes + "}\r\n";
	}
	
	private String gennerationData3Classes(Hashtable<Integer, Float> annotationSentiment, Hashtable<Integer, Float> annotationReal){
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
	
	private String gennerationData2ClassesChange(Hashtable<Integer, Float> annotationSentiment, Hashtable<Integer, Float> annotationReal){
		StringBuilder data = new StringBuilder();

		Float diffValue;
		
		for(int i = 0; i < annotationReal.size() - 1; i++){
			diffValue = annotationReal.get(i + 1) - annotationReal.get(i);
			
			data.append(annotationSentiment.get(i));
			data.append(",");
			data.append(annotationSentiment.get(i + 1));
			data.append(",");
			data.append(diffValue != 0 ? "Muda" : "Mantem");
			data.append("\n");
		}
		
		return data.toString();
	}
	
	private String gennerationData2ClassesUpDown(Hashtable<Integer, Float> annotationSentiment, Hashtable<Integer, Float> annotationReal){
		StringBuilder data = new StringBuilder();

		Float diffValue;
		
		for(int i = 0; i < annotationReal.size() - 1; i++){
			diffValue = annotationReal.get(i + 1) - annotationReal.get(i);
			
			if(diffValue == 0)
				continue;
			
			data.append(annotationSentiment.get(i));
			data.append(",");
			data.append(annotationSentiment.get(i + 1));
			data.append(",");
			data.append(diffValue > 0 ? "Aumenta" : "Diminui");
			data.append("\n");
		}
		
		return data.toString();
	}
}
