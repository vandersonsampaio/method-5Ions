package util.adjustWeight.AG;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Population {
	private int length;
	Map<Integer,Weights> population;
	Map<Integer,Weights> sons;
	HashMap<Integer, Double> prob;
    public static Double mutRate = 0.01;
    private Engine engine;
    
    public Population(Engine engine, int length){
    	this.engine = engine;
    	this.length = length;
    }
    
    public void setSons(Map<Integer, Weights> sons){
    	this.sons = sons;
    }
    
    public Map<Integer, Weights> getSons(){
    	return sons;
    }
    
    public void setPopulation(Map<Integer, Weights> population){
    	this.population = population;
    }
    
    public int getLength(){
    	return length;
    }
    
    public void startPopulation() {
    	population = new HashMap<Integer,Weights>();
		 for (int i = 0 ; i < length ; i++ )
			 population.put(i, engine.randomWeight());
	}
    
    public void probs() {
		prob = new HashMap<Integer,Double>();
		double criteriaTotal = 0;
		double range = 0;
		
		for (int i = 0 ; i < length ; i++ ){
			criteriaTotal += (population.get(i).getCriteria() > engine.getCriteria() ? engine.getCriteria() : population.get(i).getCriteria());
		}
		
		for (int i = 0; i < length; i++) {
			Double aux = (population.get(i).getCriteria() > engine.getCriteria() ? engine.getCriteria() : population.get(i).getCriteria()) / criteriaTotal;
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

		return new Parents(t1,t2);
	}
    
    public Weights mutacao(Weights wei) {
		Random rand = new Random();
		Double r1 = rand.nextDouble();
		boolean mudou = false;
		if (r1 <= mutRate) {
			do {
				double d = rand.nextFloat();
				int p = rand.nextInt(4);
				int s = rand.nextInt(2); //Defini o sinal do peso (positivo ou negativo)
				
				int i = rand.nextInt(3);
				
				double value = (d * Math.pow(10, p)) * (s == 0 ? -1 : 1);
				
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
			if (population.get(i).getCriteria() >= engine.getCriteria()) {
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
		
		System.out.println("Selecionado: {" + ret[0] + "; " + ret[1] + "; " + ret[2] + "} Critério:" + maxCriteria);
		
		return ret;
	}
}
