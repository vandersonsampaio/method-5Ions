package classify.nested;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.rules.OneR;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class NestedClassify {

	private final static int NUMITERATOR = 10000;
	
	public static void main(String[] args) throws Exception {
		
		for(int i = 50; i < 90; i++) {
			System.out.println("Percentage Split: " + i);
			trainningClassifier(i);
			System.out.println("----------------\n");
		}
	}
	
	private static void trainningClassifier(int percentageSplit) throws Exception {
		Instances dataSet = createInstances("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Research\\Weka\\LSA\\Fors\\P\\Emnid_Metric1_3Classes.arff");
		
		double[] maxAcurracy = new double[4];
		double[] minAcurracy = new double[4];
		double[] medAcurracy = new double[4];
		double[] dpAcurracy = new double[4];
		
		for(int i = 0; i < 4; i++) {
			maxAcurracy[i] = 0;
			minAcurracy[i] = 1;
			medAcurracy[i] = 0;
			dpAcurracy[i] = 0;
		}
			
		
		for(int i = 0; i < NUMITERATOR; i++) {
			Instances[] dataSets = splitDataSet(dataSet, percentageSplit);
			
			Classifier classifier3C = createClassifier(dataSets[0]);
			Classifier classifierMM = createClassifierVP(adjusteDataSetMM(dataSets[0]));
			Classifier classifierUD = createClassifier(adjusteDataSetUD(dataSets[0]));
			Nested nested = new Nested(classifierMM, classifierUD);
			
			double[] eval = evaluation(classifier3C, dataSets[1]);
			
			maxAcurracy[0] = eval[0] > maxAcurracy[0] ? eval[0] : maxAcurracy[0];
			minAcurracy[0] = eval[0] < minAcurracy[0] ? eval[0] : minAcurracy[0];
			medAcurracy[0] += eval[0];
			
			eval = evaluation(classifierMM, adjusteDataSetMM(dataSets[1]));
			
			maxAcurracy[1] = eval[0] > maxAcurracy[1] ? eval[0] : maxAcurracy[1];
			minAcurracy[1] = eval[0] < minAcurracy[1] ? eval[0] : minAcurracy[1];
			medAcurracy[1] += eval[0];
			
			eval = evaluation(classifierUD, adjusteDataSetUD(dataSets[1]));
			
			maxAcurracy[2] = eval[0] > maxAcurracy[2] ? eval[0] : maxAcurracy[2];
			minAcurracy[2] = eval[0] < minAcurracy[2] ? eval[0] : minAcurracy[2];
			medAcurracy[2] += eval[0];
			
			eval = evaluationNested(nested, dataSets[1]);
			
			maxAcurracy[3] = eval[0] > maxAcurracy[3] ? eval[0] : maxAcurracy[3];
			minAcurracy[3] = eval[0] < minAcurracy[3] ? eval[0] : minAcurracy[3];
			medAcurracy[3] += eval[0];
			
		}
		
		System.out.println("Acurácias Classificador 3C");
		System.out.println("Mínima: " + minAcurracy[0]);
		System.out.println("Máxima: " + maxAcurracy[0]);
		System.out.println("Média: " + (medAcurracy[0] / NUMITERATOR) + "\n");
		
		System.out.println("Acurácias Classificador MM");
		System.out.println("Mínima: " + minAcurracy[1]);
		System.out.println("Máxima: " + maxAcurracy[1]);
		System.out.println("Média: " + (medAcurracy[1] / NUMITERATOR) + "\n");
		
		System.out.println("Acurácias Classificador UD");
		System.out.println("Mínima: " + minAcurracy[2]);
		System.out.println("Máxima: " + maxAcurracy[2]);
		System.out.println("Média: " + (medAcurracy[2] / NUMITERATOR) + "\n");
		
		System.out.println("Acurácias Classificador Nested");
		System.out.println("Mínima: " + minAcurracy[3]);
		System.out.println("Máxima: " + maxAcurracy[3]);
		System.out.println("Média: " + (medAcurracy[3] / NUMITERATOR) + "\n");
	}
	
	private static Instances adjusteDataSetMM(Instances dataSet) {
		List<String> classes = new ArrayList<String>();
		classes.add("Muda");
		classes.add("Mantem");
		Attribute classAtt = new Attribute("class", classes);
		
		ArrayList<Attribute> a = new ArrayList<Attribute>();
		a.add(new Attribute("ini"));
		a.add(new Attribute("end"));
		a.add(classAtt);
		
		Instances data = new Instances("dataMM", a, 0);
		data.setClassIndex(data.numAttributes() - 1);
		
		
		for(int i = dataSet.numInstances() - 1; i >= 0; i--) {
			Instance ins = dataSet.get(i);
			ins.setClassValue(dataSet.get(i).classValue() < 2 ? 0 : 1);
			data.add(ins);
		}
	
		return data;
	}
	
	private static Instances adjusteDataSetUD(Instances dataSet) {
		List<String> classes = new ArrayList<String>();
		classes.add("Aumenta");
		classes.add("Diminui");
		Attribute classAtt = new Attribute("class", classes);
		
		ArrayList<Attribute> a = new ArrayList<Attribute>();
		a.add(new Attribute("ini"));
		a.add(new Attribute("end"));
		a.add(classAtt);
		
		Instances data = new Instances("dataUP", a, 0);
		data.setClassIndex(data.numAttributes() - 1);
		
		
		for(int i = dataSet.numInstances() - 1; i >= 0; i--) {
			if(dataSet.get(i).classValue() < 2)
				data.add(dataSet.get(i));
		}
		
		return data;
	}
	
	private static Classifier createClassifier(Instances dataTrainning) throws Exception {
		Classifier classifier = new OneR();
		classifier.buildClassifier(dataTrainning);
		
		return classifier;
	}
	
	private static Classifier createClassifierVP(Instances dataTrainning) throws Exception {
		Classifier classifier = new VotedPerceptron();
		classifier.buildClassifier(dataTrainning);
		
		return classifier;
	}
	
	private static Instances createInstances(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		Instances data = new Instances(reader);
		reader.close();
		
		data.setClassIndex(data.numAttributes() - 1);
		
		return data;
	}
	
	private static double[] evaluation(Classifier classifier, Instances dataTest) throws Exception {
		double[] ret = new double[2];
		Evaluation eval = new Evaluation(dataTest);
		eval.evaluateModel(classifier, dataTest);
		 
		ret[0] = 1 - eval.errorRate();
		//ret[1] = 
		
		
		return ret;
		//System.out.println("Acurácia: " + (1 - eval.errorRate()));
		
		//System.out.println(eval.toSummaryString("\nResults\n======\n", false));
	}
	
	private static double[] evaluationNested(Nested classifier, Instances dataTest) throws Exception {
		double[] ret = new double[2]; 
		int[][] matrix = new int[3][3];
		
		for(int i = 0; i < dataTest.size(); i++) {
			double realValue = dataTest.get(i).classValue();
			double classValue = classifier.classify(dataTest.get(i));
			
			matrix[(int) classValue][(int) realValue]++;
			
		}
				
		ret[0] = ((double)(matrix[0][0] + matrix[1][1] + matrix[2][2]) / dataTest.size());
		ret[1] = .0;
		
		return ret;
	}
	
	private static Instances[] splitDataSet(Instances completeDataSet, int percentageSplit) {
		Instances[] ret = new Instances[2];
				
		int numInstancesTraining = (int) Math.round(completeDataSet.numInstances() * ((double) percentageSplit / 100));
		int numInstancesTest = completeDataSet.numInstances() - numInstancesTraining;
		
		completeDataSet.randomize(new java.util.Random(0));
		
		ret[0] = new Instances(completeDataSet, 0, numInstancesTraining);
		ret[1] = new Instances(completeDataSet, numInstancesTraining, numInstancesTest);
		
		//System.out.println("Tamanho Original: " + completeDataSet.size());
		//System.out.println("Tamanho Treinamento: " + ret[0].size());
		//System.out.println("Tamanho Teste: " + ret[1].size());
		
		return ret;
	}
}

class Nested{
	
	Classifier classifier1;
	Classifier classifier2;
	
	public Nested(Classifier classifier1, Classifier classifier2) {
		this.classifier1 = classifier1;
		this.classifier2 = classifier2;
	}
	
	public double classify(Instance instance) throws Exception {
		double ret = this.classifier1.classifyInstance(instance);
		
		if(ret == 0) {
			ret = this.classifier2.classifyInstance(instance);
			return ret;
			//return ret == 0 ? "Aumenta" : "Diminui";
		} else {
			//return "Mantem";
			return 2;
		}
	}
}
