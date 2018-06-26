package util.adjustWeight.AG;

import java.util.Random;

public class Engine {
	public static double criteria = .6;
	
	public static Weights randomWeight() {
		Weights ret = new Weights();
		
		Weight[] wei = new Weight[3];
		Random rand = new Random();
		for (int i = 0; i < wei.length; i++) {
			double d = rand.nextFloat();
			int p = rand.nextInt(4);
			wei[i] = new Weight(d * Math.pow(10, p));
		}

		ret.setWeights(wei);
		return ret;
	}
	
	public boolean notDone(Population p) {
		for (int i = 0; i < Population.length; i++)
			if (p.population.get(i).getCriteria() >= criteria) 
				return false;
		
		return true;
	}
}
