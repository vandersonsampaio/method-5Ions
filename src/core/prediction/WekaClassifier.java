package core.prediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import util.commom.Files;
import util.commom.Properties;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.rules.OneR;
import weka.classifiers.trees.J48;
import weka.core.Instances;

public class WekaClassifier {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		WekaClassifier wClassifier = new WekaClassifier();
		
		Instances data = wClassifier.createInstantes("");

		Classifier classifier = new J48();
		classifier.buildClassifier(data);
		
		wClassifier.evaluation(classifier, data);
		
		classifier = new OneR();
		classifier.buildClassifier(data);
		
		wClassifier.evaluation(classifier, data);
	}

	
	public double calculeAcurracy(String fileName) throws Exception{
		Instances data = this.createInstantes(fileName + ".arff");
		
		Classifier classifier = new OneR();
		classifier.buildClassifier(data);
		
		return evaluation(classifier, data);
	}
	
	public void calculeAcurracy() throws Exception{
		List<String> listNames = Files.getAllFileNames(Properties.getProperty("pathFolderWeka"));
		Instances data;
		
		for(int i = 0; i < listNames.size(); i++){
			System.out.print(listNames.get(i) + " ");
			data = this.createInstantes(listNames.get(i));
			
			Classifier classifier = new J48();
			classifier.buildClassifier(data);
			
			System.out.println("Acurária: " + evaluation(classifier, data));
		}
	}
	
	private Instances createInstantes(String fileName) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(Properties.getProperty("pathFolderWeka") + File.separator + fileName));
		
		Instances data = new Instances(reader);
		reader.close();
		
		// setting class attribute
		data.setClassIndex(data.numAttributes() - 1);
		
		return data;
	}
	
	private double evaluation(Classifier model, Instances data) throws Exception{
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(model, data, 10, new Random(1));
		 
		//System.out.println("Acurácia: " + (1 - eval.errorRate()));
		
		return 1 - eval.errorRate();
		//System.out.println(eval.toSummaryString("\nResults\n======\n", false));
	}
}
