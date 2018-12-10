package core.correlation;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import util.adjustWeight.AG.Crossover;
import util.adjustWeight.AG.Engine;
import util.adjustWeight.AG.Parents;
import util.adjustWeight.AG.Population;
import util.adjustWeight.AG.Weights;
import util.commom.Dates;

public class CalculateWeight {

	private final int NUMBERPOPULATION = 1000;
	public static Hashtable<String, double[][]> constants;
	public static Set<String> names;
	public static boolean correlation;
	private int generations;
	private double criteria;

	public CalculateWeight(Set<String> names, Hashtable<String, double[][]> constants) {
		CalculateWeight.constants = constants;
		CalculateWeight.names = names;
		this.generations = 100;
		this.criteria = .6;
	}

	public CalculateWeight(int generations, double criteria) {
		this.generations = generations;
		this.criteria = criteria;
	}

	public double[] calculateWeigth() {
		int numberWeights = 3;
		System.out.println("Inicio");
		Engine engine = new Engine(criteria, numberWeights);
		Population populacao = new Population(engine, NUMBERPOPULATION);

		populacao.startPopulation();
		populacao.probs();

		int geracao = 0;
		while (engine.notDone(populacao) && geracao <= generations) {
			System.out.println("Geração: " + (geracao++));
			populacao.setSons(new HashMap<Integer, Weights>());

			for (int i = 0; i < populacao.getLength() / 2; i++) {

				Parents pais = populacao.selecao();
				populacao.getSons().put(2 * i, populacao.mutacao(Crossover.crossover(pais)));
				populacao.getSons().put(2 * i + 1, populacao.mutacao(Crossover.crossover(pais)));
			}

			populacao.setPopulation(populacao.getSons());
			populacao.probs();
		}

		return populacao.printBestWeights(geracao, numberWeights);
	}

	public double[] calculateWeigth(BasicDBList entities, Hashtable<Date, Double> dataExt) {
		int numberWeights = entities.size();

		Date maxDate;
		Date minDate = maxDate = dataExt.keys().nextElement();

		for (Date d : dataExt.keySet()) {
			if (d.before(minDate))
				minDate = d;
			else if (d.after(maxDate))
				maxDate = d;
		}

		Engine.constants = new double[dataExt.size()][entities.size()];
		Engine.indicatorExternal = new double[dataExt.size()];

		Date d = minDate;
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		
		int indexDate = 0;
		while (d.before(maxDate) || d.equals(maxDate)) {
			if (dataExt.containsKey(d)) {
				Engine.indicatorExternal[indexDate] = dataExt.get(d);

				for (int j = 0; j < entities.size(); j++) {
					Engine.constants[indexDate][j] = 0;
					BasicDBList metric = (BasicDBList) ((BasicDBObject) ((BasicDBObject) entities.get(j)).get("metric"))
							.get("values_acum");

					for (int k = 0; k < metric.size(); k++) {
						if (Dates.getZeroTimeDate(((BasicDBObject) metric.get(k)).getDate("date")).equals(Dates.getZeroTimeDate(d))) {
							Engine.constants[indexDate][j] = ((BasicDBObject) metric.get(k)).getDouble("value_direct");
							break;
						}
					}
				}

				indexDate++;
			}

			c.add(Calendar.DATE, 1);
			d = Dates.getZeroTimeDate(c.getTime());
		}

		Engine engine = new Engine(criteria, numberWeights);
		Population populacao = new Population(engine, NUMBERPOPULATION);

		populacao.startPopulation();
		populacao.probs();

		int generation = 0;
		while (engine.notDone(populacao) && generation <= generations) {
			System.out.println("Generation: " + (generation++));
			populacao.setSons(new HashMap<Integer, Weights>());

			for (int i = 0; i < populacao.getLength() / 2; i++) {

				Parents pais = populacao.selecao();
				populacao.getSons().put(2 * i, populacao.mutacao(Crossover.crossover(pais)));
				populacao.getSons().put(2 * i + 1, populacao.mutacao(Crossover.crossover(pais)));
			}

			populacao.setPopulation(populacao.getSons());
			populacao.probs();
		}

		return populacao.printBestWeights(generation, numberWeights);
	}
}