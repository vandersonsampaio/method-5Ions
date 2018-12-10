package util.adjustWeight.AG;

import java.util.Random;

public class Engine {
	public static double[][] constants;
	public static double[] indicatorExternal;
	private double criteria;
	private int numberWeights;
	
	public Engine(double criteria, int numberWeights){
		this.criteria = criteria;
		this.numberWeights = numberWeights;
	}
	
	public Weights randomWeight() {
		Weights ret = new Weights(numberWeights);
		
		Weight[] wei = new Weight[numberWeights];
		Random rand = new Random();
		for (int i = 0; i < wei.length; i++) {
			double d = rand.nextFloat();
			int p = rand.nextInt(4);
			int s = rand.nextInt(2); //Defini o sinal do peso (positivo ou negativo)
			
			wei[i] = new Weight((d * Math.pow(10, p)) * (s == 0 ? -1 : 1));
		}

		ret.setWeights(wei);
		return ret;
	}
	
	public boolean notDone(Population p) {
		for (int i = 0; i < p.getLength(); i++)
			if (p.population.get(i).getCriteria() >= criteria) 
				return false;
		
		return true;
	}
	
	public double getCriteria(){
		return criteria;
	}
}
