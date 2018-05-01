package core.entity;

import org.joda.time.DateTime;

public class ExternalData {

	private DateTime publicationDate;
	private DateTime initialDate;
	private DateTime endDate;
	private float value;
	
	public DateTime getPublicationDate() {
		return publicationDate;
	}
	
	public void setPublicationDate(DateTime publicationDate) {
		this.publicationDate = publicationDate;
	}
	
	public DateTime getInitialDate() {
		return initialDate;
	}
	
	public void setInitialDate(DateTime initialDate) {
		this.initialDate = initialDate;
	}
	
	public DateTime getEndDate() {
		return endDate;
	}
	
	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}
	
	public float getValue() {
		return value;
	}
	
	public void setValue(float value) {
		this.value = value;
	}
}
