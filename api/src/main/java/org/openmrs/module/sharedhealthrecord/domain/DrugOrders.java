package org.openmrs.module.sharedhealthrecord.domain;

public class DrugOrders {
	
	private String careSetting;
	
	private int duration;
	
	private String durationUnits;
	
	private String scheduledDate;
	
	private String autoExpireDate;
	
	private String dateStopped;
	
	private String dosingInstructionType;
	
	private String orderType;
	
	private Drug drug;
	
	private DosingInstructions dosingInstructions;
	
	public String getCareSetting() {
		return careSetting;
	}
	
	public void setCareSetting(String careSetting) {
		this.careSetting = careSetting;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public String getDurationUnits() {
		return durationUnits;
	}
	
	public void setDurationUnits(String durationUnits) {
		this.durationUnits = durationUnits;
	}
	
	public String getScheduledDate() {
		return scheduledDate;
	}
	
	public void setScheduledDate(String scheduledDate) {
		this.scheduledDate = scheduledDate;
	}
	
	public String getAutoExpireDate() {
		return autoExpireDate;
	}
	
	public void setAutoExpireDate(String autoExpireDate) {
		this.autoExpireDate = autoExpireDate;
	}
	
	public String getDateStopped() {
		return dateStopped;
	}
	
	public void setDateStopped(String dateStopped) {
		this.dateStopped = dateStopped;
	}
	
	public String getDosingInstructionType() {
		return dosingInstructionType;
	}
	
	public void setDosingInstructionType(String dosingInstructionType) {
		this.dosingInstructionType = dosingInstructionType;
	}
	
	public String getOrderType() {
		return orderType;
	}
	
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	public Drug getDrug() {
		return drug;
	}
	
	public void setDrug(Drug drug) {
		this.drug = drug;
	}
	
	public DosingInstructions getDosingInstructions() {
		return dosingInstructions;
	}
	
	public void setDosingInstructions(DosingInstructions dosingInstructions) {
		this.dosingInstructions = dosingInstructions;
	}
	
}
