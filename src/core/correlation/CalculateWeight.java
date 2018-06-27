package core.correlation;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import util.adjustWeight.AG.Crossover;
import util.adjustWeight.AG.Engine;
import util.adjustWeight.AG.Parents;
import util.adjustWeight.AG.Population;
import util.adjustWeight.AG.Weights;

public class CalculateWeight {
	public static Hashtable<String, double[][]> constants;
	public static Set<String> names;
	
	public CalculateWeight(Set<String> names, Hashtable<String, double[][]> constants){
		CalculateWeight.constants = constants;
		CalculateWeight.names = names;
	}

	public double[] calculateWeigth(){
		System.out.println("Inicio");
		Engine engine = new Engine();
		Population populacao = new Population(); //Necessário criar valores negativos
		
		populacao.iniciaPopulacao();
		populacao.probs();
		
		int geracao = 0;
		while(engine.notDone(populacao) && geracao <= 100){
			System.out.println("Geração: " + (geracao++));
			populacao.setSons(new HashMap<Integer, Weights>());
			
			for(int i = 0 ; i < Population.length / 2 ; i++) {
				
				Parents pais = populacao.selecao();
				populacao.getSons().put(2 * i, populacao.mutacao(Crossover.crossover(pais)));
				populacao.getSons().put(2* i + 1, populacao.mutacao(Crossover.crossover(pais)));
			}
			
			populacao.setPopulation(populacao.getSons());
			populacao.probs();
		}
		
		return populacao.printBestWeights(geracao);
	}
}