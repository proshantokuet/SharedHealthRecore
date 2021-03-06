package org.openmrs.module.sharedhealthrecord.domain;

public class ObservationWithGroupMemebrs {
	
	private Concept concept;
	
	private String formNamespace;
	
	private String formFieldPath;
	
	//private List<GroupMember> groupMembers;
	
	private boolean voided;
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public String getFormNamespace() {
		return formNamespace;
	}
	
	public void setFormNamespace(String formNamespace) {
		this.formNamespace = formNamespace;
	}
	
	public String getFormFieldPath() {
		return formFieldPath;
	}
	
	public void setFormFieldPath(String formFieldPath) {
		this.formFieldPath = formFieldPath;
	}

	public boolean isVoided() {
		return voided;
	}

	public void setVoided(boolean voided) {
		this.voided = voided;
	}
	

	
}
