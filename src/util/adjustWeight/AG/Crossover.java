package util.adjustWeight.AG;

import java.util.Random;

public class Crossover {
	public static Weights crossover(Parents pais) {
		Weights filho = new Weights();
		Weight[] weight = new Weight[pais.pai1.getWeights().length];
		
		int[] cromossomo = new int[9];
		Random rand = new Random();
		int line = rand.nextInt(8);
		int r = rand.nextInt(2);
		
		for(int j = 0; j < weight.length; j++){
			for (int i = 0 ; i < line ; i++) {
				if (r == 0)
					cromossomo[i] = pais.pai1.getWeights()[j].getCromossomo()[i];
				else
					cromossomo[i] = pais.pai2.getWeights()[j].getCromossomo()[i];
			}
			r = rand.nextInt(2);
			for (int i = line ; i < 9 ; i++) {
				if (r == 0)
					cromossomo[i] = pais.pai1.getWeights()[j].getCromossomo()[i];
				else
					cromossomo[i] = pais.pai2.getWeights()[j].getCromossomo()[i];
			}
			
			weight[j] = new Weight(cromossomo);
		}
		
		filho.setWeights(weight);

		return filho;
	}
}

