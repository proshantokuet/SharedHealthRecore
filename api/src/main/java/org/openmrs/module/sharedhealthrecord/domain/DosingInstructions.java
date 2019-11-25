package org.openmrs.module.sharedhealthrecord.domain;

public class DosingInstructions {
	
	private int dose;
	
	private String doseUnits;
	
	private String route;
	
	private String frequency;
	
	private String asNeeded;
	
	private String administrationInstructions;
	
	private int quantity;
	
	private String quantityUnits;
	
	private int numberOfRefills;
	
	public int getDose() {
		return dose;
	}
	
	public void setDose(int dose) {
		this.dose = dose;
	}
	
	public String getDoseUnits() {
		return doseUnits;
	}
	
	public void setDoseUnits(String doseUnits) {
		this.doseUnits = doseUnits;
	}
	
	public String getRoute() {
		return route;
	}
	
	public void setRoute(String route) {
		this.route = route;
	}
	
	public String getFrequency() {
		return frequency;
	}
	
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	
	public String getAsNeeded() {
		return asNeeded;
	}
	
	public void setAsNeeded(String asNeeded) {
		this.asNeeded = asNeeded;
	}
	
	public String getAdministrationInstructions() {
		return administrationInstructions;
	}
	
	public void setAdministrationInstructions(String administrationInstructions) {
		this.administrationInstructions = administrationInstructions;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public String getQuantityUnits() {
		return quantityUnits;
	}
	
	public void setQuantityUnits(String quantityUnits) {
		this.quantityUnits = quantityUnits;
	}
	
	public int getNumberOfRefills() {
		return numberOfRefills;
	}
	
	public void setNumberOfRefills(int numberOfRefills) {
		this.numberOfRefills = numberOfRefills;
	}
	
}
