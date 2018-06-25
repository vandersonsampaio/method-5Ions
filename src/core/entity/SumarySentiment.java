package core.entity;

public class SumarySentiment {

	private int number;
	private double[] values;
	
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public double[] getValues() {
		return values;
	}
	
	public void setValues(double[] values) {
		this.values = values;
	}
	
	public double getValueIndex(int index){
		return this.values[index];
	}
	
	public void setValueIndex(int index, int value){
		this.values[index] = value;
	}
}
