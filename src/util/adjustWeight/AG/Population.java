package util.adjustWeight.AG;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Population {
	public static int length = 100;
	Map<Integer,Weights> population;
	Map<Integer,Weights> sons;
	HashMap<Integer, Double> prob;
    public static Double mutRate = 0.01;
    
    public void setSons(Map<Integer, Weights> sons){
    	this.sons = sons;
    }
    
    public Map<Integer, Weights> getSons(){
    	return sons;
    }
    
    public void setPopulation(Map<Integer, Weights> population){
    	this.population = population;
    }
    
    public void iniciaPopulacao() {
    	population = new HashMap<Integer,Weights>();
		 for (int i = 0 ; i < length ; i++ )
			 population.put(i, Engine.randomWeight());
	}
    
    public void probs() {
		prob = new HashMap<Integer,Double>();
		int criteriaTotal = 0;
		double range = 0;
		
		for (int i = 0 ; i < length ; i++ )
			criteriaTotal += population.get(i).getCriteria();
		
		for (int i = 0; i < length; i++) {
			Double aux = ((double)population.get(i).getCriteria()) / criteriaTotal;
			prob.put(i, aux+range);
			range += aux;
		}
	}
    
    public Parents selecao() {
		Random rand = new Random();
		double r = rand.nextDouble();
		Weights t1 = null, t2 = null;
		int i;
		for (i = 0 ; i < length ; i++ ) {
			if (r <= prob.get(i)) {
				t1 = population.get(i);
				break;
			}
		}

		int j;
		do {
			r = rand.nextDouble();
			for (j = 0 ; j < length ; j++ ) {
				if (r <= prob.get(j) && i != j) {
					t2 = population.get(j);
					break;
				}
			}
		} while (j == i || t2 == null);

		System.out.println(t1);
		System.out.println(t2);
		Parents pais = new Parents(t1,t2);

		return pais;

	}
    
    public Weights mutacao(Weights wei) {
		Random rand = new Random();
		Double r1 = rand.nextDouble();
		boolean mudou = false;
		if (r1 <= mutRate) {
			do {
				double d = rand.nextFloat();
				int p = rand.nextInt(4);
				
				int i = rand.nextInt(3);
				
				double value = d * Math.pow(10, p);
				
				System.out.println(value);
				System.out.println(i);
				System.out.println(wei);
				
				if (!String.format("%.5f", wei.getWeights()[i].getValue()).equals(String.format("%.5f", value))) {
					wei.getWeights()[i].setValue(value);
					mudou = true;
				}
			} while(!mudou);

		}
		return wei;
	}
    
    public double[] printBestWeights(int geracao) {
    	double[] ret = new double[3];
    	double maxCriteria = 0;
		for (int i = 0; i < length; i++) {
			if (population.get(i).getCriteria() >= Engine.criteria) {
				System.out.println("Solução nº " + i + " da geração " + geracao);
				System.out.println("Pesos: " + population.get(i).getWeights().toString());				
			}
			
			if(population.get(i).getCriteria() > maxCriteria){
				maxCriteria = population.get(i).getCriteria();
				ret[0] = population.get(i).getWeights()[0].getValue();
				ret[1] = population.get(i).getWeights()[1].getValue();
				ret[2] = population.get(i).getWeights()[2].getValue();
			}
		}
		
		return ret;
	}
}
