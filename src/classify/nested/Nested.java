package classify.nested;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.rules.OneR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class Nested implements Classifier, java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Classifier classifier1;
	Classifier classifier2;

	public Nested(){
		//System.out.println("Nested");
	}
	
	@Override
	public void buildClassifier(Instances arg0) throws Exception {
		//classifier1 = new VotedPerceptron();
		classifier1 = new OneR();
		classifier1.buildClassifier(adjusteDataSetMM(arg0));

		//this.classifier2 = new OneR();
		this.classifier2 = new VotedPerceptron();
		classifier2.buildClassifier(adjusteDataSetUD(arg0));
	}

	@Override
	public double classifyInstance(Instance arg0) throws Exception {
		double ret = this.classifier1.classifyInstance(arg0);

		if (ret == 0) {
			ret = this.classifier2.classifyInstance(arg0);
			return ret;
			// return ret == 0 ? "Aumenta" : "Diminui";
		} else {
			// return "Mantem";
			return 2;
		}
	}

	@Override
	public double[] distributionForInstance(Instance arg0) throws Exception {
		// TODO Auto-generated method stub
		double classify = this.classifyInstance(arg0);
		
		double[] ret0 = {1.0, 0, 0};
		double[] ret1 = {0, 1, 0};
		double[] ret2 = {0, 0, 1};
		
		if(classify == 0)
			return ret0;
		else if(classify == 1)
			return ret1;
		else
			return ret2;

	}

	@Override
	public Capabilities getCapabilities() {

		return null;
	}

	private Instances adjusteDataSetMM(Instances dataSet) {
		
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

		for (int i = dataSet.numInstances() - 1; i >= 0; i--) {
			Instance ins = (Instance) dataSet.get(i).copy();
			ins.setClassValue(dataSet.get(i).classValue() < 2 ? 0 : 1);
			data.add(ins);
		}

		return data;
	}

	private Instances adjusteDataSetUD(Instances dataSet) {
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

		for (int i = dataSet.numInstances() - 1; i >= 0; i--) {
			if (dataSet.get(i).classValue() < 2)
				data.add((Instance) dataSet.get(i).copy());
		}

		return data;
	}
}
