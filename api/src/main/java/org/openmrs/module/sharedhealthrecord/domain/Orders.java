package org.openmrs.module.sharedhealthrecord.domain;

import org.openmrs.module.sharedhealthrecord.orders.Concept;

public class Orders {
	
	private Concept concept;
	
	private String action;
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}


	
}
