package es.weso.amg.recommender.model;

import org.codehaus.jackson.annotate.JsonAutoDetect;

@JsonAutoDetect
public class Media {
	private String name;
	private double trustworthiness;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getTrustworthiness() {
		return trustworthiness;
	}

	public void setTrustworthiness(double trustworthiness) {
		this.trustworthiness = trustworthiness;
	}
}
