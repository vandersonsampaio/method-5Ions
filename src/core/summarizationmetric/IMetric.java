package core.summarizationmetric;

public interface IMetric {

	public String name();
	public String description();
	public double calculatedMetric(double positive, double negative); 
}
