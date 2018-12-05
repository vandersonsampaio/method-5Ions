package core.summarizationmetric;

public interface IMetric {

	public String name();
	public String description();
	public Double calculatedMetric(double positive, double negative); 
}
