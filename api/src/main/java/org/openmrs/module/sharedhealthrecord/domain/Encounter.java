package org.openmrs.module.sharedhealthrecord.domain;

import java.util.List;

public class Encounter {
	
	private String locationUuid;
	
	private String patientUuid;
	
	private String visitUuid;
	
	private String encounterDateTime;
	
	private String visitType;
	
	private String visitTypeUuid;
	
	private String encounterTypeUuid;
	
	
	private String encounterUuid;
	
	private List<Orders> orders;
	
	private List<Provider> providers;
	
	private List<DrugOrders> drugOrders;
	
	private List<BahmniDiagnoses> bahmniDiagnoses;
	
	public String getLocationUuid() {
		return locationUuid;
	}
	
	public void setLocationUuid(String locationUuid) {
		this.locationUuid = locationUuid;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getVisitUuid() {
		return visitUuid;
	}
	
	public void setVisitUuid(String visitUuid) {
		this.visitUuid = visitUuid;
	}
	
	public String getEncounterDateTime() {
		return encounterDateTime;
	}
	
	public void setEncounterDateTime(String encounterDateTime) {
		this.encounterDateTime = encounterDateTime;
	}
	
	public String getVisitType() {
		return visitType;
	}
	
	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}
	
	public String getVisitTypeUuid() {
		return visitTypeUuid;
	}

	public void setVisitTypeUuid(String visitTypeUuid) {
		this.visitTypeUuid = visitTypeUuid;
	}

	public String getEncounterTypeUuid() {
		return encounterTypeUuid;
	}

	public void setEncounterTypeUuid(String encounterTypeUuid) {
		this.encounterTypeUuid = encounterTypeUuid;
	}

	public String getEncounterUuid() {
		return encounterUuid;
	}
	
	public void setEncounterUuid(String encounterUuid) {
		this.encounterUuid = encounterUuid;
	}
	
	public List<Orders> getOrders() {
		return orders;
	}
	
	public void setOrders(List<Orders> orders) {
		this.orders = orders;
	}
	
	public List<Provider> getProviders() {
		return providers;
	}
	
	public void setProviders(List<Provider> providers) {
		this.providers = providers;
	}
	
	public List<DrugOrders> getDrugOrders() {
		return drugOrders;
	}
	
	public void setDrugOrders(List<DrugOrders> drugOrders) {
		this.drugOrders = drugOrders;
	}

	public List<BahmniDiagnoses> getBahmniDiagnoses() {
		return bahmniDiagnoses;
	}

	public void setBahmniDiagnoses(List<BahmniDiagnoses> bahmniDiagnoses) {
		this.bahmniDiagnoses = bahmniDiagnoses;
	}
	
}
