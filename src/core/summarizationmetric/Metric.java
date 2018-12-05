package core.summarizationmetric;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Metric implements IMetric {

	public static void main(String[] args) {
		 try {
			Class<?> metricClass = Class.forName("core.summarizationmetric.Metric");

			Object metricObject = metricClass.newInstance();
			Method metricMethod = metricClass.getMethod("calculatedMetric", double.class, double.class);
			
			double ret = (double) metricMethod.invoke(metricObject, 1,2);
			System.out.println(ret);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String name() {
		return "MetricDivision";
	}

	@Override
	public String description() {
		return "division of positive sentiment by negative sentiment";
	}

	@Override
	public Double calculatedMetric(double positive, double negative) {
		if(negative == 0)
			return null;
		return (positive + negative) / negative;
	}
}
