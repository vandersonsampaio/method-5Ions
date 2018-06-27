package util.adjustWeight.AG;

import java.io.File;
import java.util.Hashtable;

import core.correlation.CalculateWeight;
import core.prediction.AdapterWeka;
import util.commom.Properties;

public class Weights {

	private Weight[] weights;
	private double criteria;
	
	public Weights(){
		this.weights = new Weight[3];
		this.criteria = 0;
	}
	
	public Weights(Weight[] weights){
		this.setWeights(weights);
	}
	
	public void setWeights(Weight[] weights){
		this.weights = weights;
		
		Hashtable<String, double[]> values = new Hashtable<>(); 
		
		for(String name : CalculateWeight.names){
			double[][] constant = CalculateWeight.constants.get(name);
			double[] value = new double[constant.length];
			
			for(int i = 0; i < constant.length; i++){
				value[i] = weights[0].getValue() * constant[i][0] + weights[1].getValue() * constant[i][1] + weights[2].getValue() * constant[i][2];
			}
			
			values.put(name, value);
		}
		
		AdapterWeka adWeka = new AdapterWeka();
		String fileName = adWeka.gennerationTempARFF(CalculateWeight.names, values);
		
		this.criteria = adWeka.calculeAccuracy(fileName);
		
		File file = new File(Properties.getProperty("pathFolderWeka") + File.separator + fileName + ".arff");
		
		if(file.isFile()){
			file.delete();
		}
	}
	
	public Weight[] getWeights(){
		return weights;
	}
	
	public double getCriteria(){
		return criteria;
	}
	
	@Override
	public String toString(){
		return "{" + weights[0].getValue() + "; " + weights[1].getValue() + "; " + weights[2].getValue() + "} - Critério: " + criteria;
	}
}

class Weight {
	
	private double value;
	private int[] cromossomo;
	
	public Weight(double value){
		this.cromossomo = new int[9];
		setValue(value);
	}
	
	public Weight(int[] cromossomo){
		this.cromossomo = cromossomo;
		
		String value = cromossomo[0] + "" +  cromossomo[1] + "" + cromossomo[2] + "." + cromossomo[3] + "" + cromossomo[4] + "" + 
				cromossomo[5] + "" + cromossomo[6] + "" + cromossomo[7];
		
		this.value = Double.parseDouble(value) * cromossomo[8];
	}
	
	public void setValue(double value){
		this.value = value;
		
		setCromossomo();
	}
	
	public double getValue(){
		return value;
	}
	
	public int[] getCromossomo(){
		return cromossomo;
	}
	
	private void setCromossomo(){
		Integer partInt = (int) (Math.floor(Math.abs(value) * 100) / 100);
		Double partDecimal = (Math.abs(value) - partInt);
		
		String sPartInt = partInt.toString();
		String sPartDecimal = String.format("%.5f", partDecimal);
		
		if(sPartInt.length() == 3){
			cromossomo[0] = Integer.parseInt(sPartInt.charAt(0) + "");
			cromossomo[1] = Integer.parseInt(sPartInt.charAt(1) + "");
			cromossomo[2] = Integer.parseInt(sPartInt.charAt(2) + "");
		} else if(sPartInt.length() == 2) {
			cromossomo[0] = 0;
			cromossomo[1] = Integer.parseInt(sPartInt.charAt(0) + "");
			cromossomo[2] = Integer.parseInt(sPartInt.charAt(1) + "");
		} else if(sPartInt.length() == 1) {
			cromossomo[0] = 0;
			cromossomo[1] = 0;
			cromossomo[2] = Integer.parseInt(sPartInt.charAt(0) + "");
		}
		
		cromossomo[3] = Integer.parseInt(sPartDecimal.charAt(2) + "");
		cromossomo[4] = Integer.parseInt(sPartDecimal.charAt(3) + "");
		cromossomo[5] = Integer.parseInt(sPartDecimal.charAt(4) + "");
		cromossomo[6] = Integer.parseInt(sPartDecimal.charAt(5) + "");
		cromossomo[7] = Integer.parseInt(sPartDecimal.charAt(6) + "");
		cromossomo[8] = value > 0 ? 1 : -1;
		
		//System.out.println(cromossomo[0] + "" +  cromossomo[1] + "" + cromossomo[2] + "," + cromossomo[3] + "" + cromossomo[4] + "" + 
		//		cromossomo[5] + "" + cromossomo[6] + "" + cromossomo[7]);
	}
}