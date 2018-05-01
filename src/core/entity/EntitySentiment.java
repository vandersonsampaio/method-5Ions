package core.entity;

public class EntitySentiment {

	private String entityName;
	private float positiveSentimentDM;
	private float negativaSentimentDM;
	private float positiveSentimentCM;
	private float negativaSentimentCM;
	private int numberDirectMentions;
	private int numberCoMentions;
	
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public float getPositiveSentimentDM() {
		return positiveSentimentDM;
	}
	public void setPositiveSentimentDM(float positiveSentimentDM) {
		this.positiveSentimentDM = positiveSentimentDM;
	}
	public float getNegativaSentimentDM() {
		return negativaSentimentDM;
	}
	public void setNegativaSentimentDM(float negativaSentimentDM) {
		this.negativaSentimentDM = negativaSentimentDM;
	}
	public float getPositiveSentimentCM() {
		return positiveSentimentCM;
	}
	public void setPositiveSentimentCM(float positiveSentimentCM) {
		this.positiveSentimentCM = positiveSentimentCM;
	}
	public float getNegativaSentimentCM() {
		return negativaSentimentCM;
	}
	public void setNegativaSentimentCM(float negativaSentimentCM) {
		this.negativaSentimentCM = negativaSentimentCM;
	}
	public int getNumberDirectMentions() {
		return numberDirectMentions;
	}
	public void setNumberDirectMentions(int numberDirectMentions) {
		this.numberDirectMentions = numberDirectMentions;
	}
	public int getNumberCoMentions() {
		return numberCoMentions;
	}
	public void setNumberCoMentions(int numberCoMentions) {
		this.numberCoMentions = numberCoMentions;
	}
	
	@Override
	public String toString() {
		return "EntitySentiment [entityName=" + entityName + ", postiveSentimentDM=" + positiveSentimentDM
				+ ", negativaSentimentDM=" + negativaSentimentDM + ", postiveSentimentCM=" + positiveSentimentCM
				+ ", negativaSentimentCM=" + negativaSentimentCM + ", numberDirectMentions=" + numberDirectMentions
				+ ", numberCoMentions=" + numberCoMentions + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityName == null) ? 0 : entityName.hashCode());
		result = prime * result + Float.floatToIntBits(negativaSentimentDM);
		result = prime * result + Float.floatToIntBits(negativaSentimentCM);
		result = prime * result + numberCoMentions;
		result = prime * result + numberDirectMentions;
		result = prime * result + Float.floatToIntBits(positiveSentimentDM);
		result = prime * result + Float.floatToIntBits(positiveSentimentCM);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntitySentiment other = (EntitySentiment) obj;
		if (entityName == null) {
			if (other.entityName != null)
				return false;
		} else if (!entityName.equals(other.entityName))
			return false;
		if (Float.floatToIntBits(negativaSentimentDM) != Float.floatToIntBits(other.negativaSentimentDM))
			return false;
		if (Float.floatToIntBits(negativaSentimentCM) != Float.floatToIntBits(other.negativaSentimentCM))
			return false;
		if (numberCoMentions != other.numberCoMentions)
			return false;
		if (numberDirectMentions != other.numberDirectMentions)
			return false;
		if (Float.floatToIntBits(positiveSentimentDM) != Float.floatToIntBits(other.positiveSentimentDM))
			return false;
		if (Float.floatToIntBits(positiveSentimentCM) != Float.floatToIntBits(other.positiveSentimentCM))
			return false;
		return true;
	}
	
}
