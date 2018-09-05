package classify.nested;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.rules.OneR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class NestedClassify {

	public static void main(String[] args) throws Exception {

		trainningClassifier();
	}

	private static void trainningClassifier() throws Exception {
		Instances dataSet = createInstances(
				"C:\\Users\\vanderson.sampaio\\Documents\\PSS\\Research\\Weka\\Fors\\P\\Emnid_Metric1_3Classes.arff");

		Classifier classifier3C = createClassifier(dataSet);
		Classifier nested = new Nested();
		nested.buildClassifier(dataSet);

		double eval = evaluation(classifier3C, dataSet);
		System.out.println("3 Classes " + eval);

		
		
		//for(int i = 0; i < dataSet.numInstances(); i++) {
		//	System.out.println(dataSet.get(i));
		//	System.out.println("" + Arrays.toString(classifier3C.distributionForInstance(dataSet.get(i))));
		//	System.out.println("-- " + Arrays.toString(nested.distributionForInstance(dataSet.get(i))));
		//}
		eval = evaluation(nested, dataSet);
		System.out.println("Nested " + eval);

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

	private static double evaluation(Classifier classifier, Instances dataTest) throws Exception {

		Evaluation eval = new Evaluation(dataTest);
		eval.crossValidateModel(classifier, dataTest, 10, new Random(1));

		return 1 - eval.errorRate();
	}

}