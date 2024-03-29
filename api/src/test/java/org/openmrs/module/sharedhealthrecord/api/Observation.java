package org.openmrs.module.sharedhealthrecord.api;

public class Observation {
	
	private Concept concept;
	
	private String formNamespace;
	
	private String formFieldPath;
	
	private String value;
	
	private Boolean inactive = false;
	
	private String encounterUuid;
	
	private String observationDateTime;
	
	private String encounterDateTime;
	
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
	
	public Boolean getInactive() {
		return inactive;
	}
	
	public void setInactive(Boolean inactive) {
		this.inactive = inactive;
	}
	
	public String getEncounterUuid() {
		return encounterUuid;
	}
	
	public void setEncounterUuid(String encounterUuid) {
		this.encounterUuid = encounterUuid;
	}
	
	public String getObservationDateTime() {
		return observationDateTime;
	}
	
	public void setObservationDateTime(String observationDateTime) {
		this.observationDateTime = observationDateTime;
	}
	
	public String getEncounterDateTime() {
		return encounterDateTime;
	}
	
	public void setEncounterDateTime(String encounterDateTime) {
		this.encounterDateTime = encounterDateTime;
	}
	
}
