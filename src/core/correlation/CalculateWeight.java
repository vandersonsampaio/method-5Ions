package core.correlation;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import com.mongodb.BasicDBList;

import util.adjustWeight.AG.Crossover;
import util.adjustWeight.AG.Engine;
import util.adjustWeight.AG.Parents;
import util.adjustWeight.AG.Population;
import util.adjustWeight.AG.Weights;

public class CalculateWeight {

	private final int NUMBERPOPULATION = 5000;
	public static Hashtable<String, double[][]> constants;
	public static Set<String> names;
	private int generations;
	private double criteria;
	
	public CalculateWeight(Set<String> names, Hashtable<String, double[][]> constants){
		CalculateWeight.constants = constants;
		CalculateWeight.names = names;
		this.generations = 100;
		this.criteria = 6;
	}
	
	public CalculateWeight(int generations, double criteria){
		this.generations = generations;
		this.criteria = criteria;
	}

	public double[] calculateWeigth(){
		System.out.println("Inicio");
		Engine engine = new Engine(criteria);
		Population populacao = new Population(engine, NUMBERPOPULATION);
		
		populacao.startPopulation();
		populacao.probs();
		
		int geracao = 0;
		while(engine.notDone(populacao) && geracao <= generations){
			System.out.println("Geração: " + (geracao++));
			populacao.setSons(new HashMap<Integer, Weights>());
			
			for(int i = 0 ; i < populacao.getLength() / 2 ; i++) {
				
				Parents pais = populacao.selecao();
				populacao.getSons().put(2 * i, populacao.mutacao(Crossover.crossover(pais)));
				populacao.getSons().put(2* i + 1, populacao.mutacao(Crossover.crossover(pais)));
			}
			
			populacao.setPopulation(populacao.getSons());
			populacao.probs();
		}
		
		return populacao.printBestWeights(geracao);
	}
	
	public double[] calculateWeigth(BasicDBList entities, Hashtable<Date, Double> dataExt){
		return calculateWeigth();
	}
}