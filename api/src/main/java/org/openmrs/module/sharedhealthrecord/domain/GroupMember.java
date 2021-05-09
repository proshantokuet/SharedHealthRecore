package org.openmrs.module.sharedhealthrecord.domain;

import java.util.List;

public class GroupMember {
	
	private Concept concept;
	
	private String formNamespace;
	
	private String formFieldPath;
	
	private String value;
	
	private boolean voided;
	
	private List<GroupMember> groupMembers;
	
	public List<GroupMember> getGroupMembers() {
		return groupMembers;
	}
	
	public void setGroupMembers(List<GroupMember> groupMembers) {
		this.groupMembers = groupMembers;
	}
	
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
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public boolean isVoided() {
		return voided;
	}

	public void setVoided(boolean voided) {
		this.voided = voided;
	}
	
}
