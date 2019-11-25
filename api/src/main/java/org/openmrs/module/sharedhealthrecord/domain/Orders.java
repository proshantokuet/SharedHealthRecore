package org.openmrs.module.sharedhealthrecord.domain;

import org.openmrs.module.sharedhealthrecord.orders.Concept;

public class Orders {
	
	private Concept concept;
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
}
