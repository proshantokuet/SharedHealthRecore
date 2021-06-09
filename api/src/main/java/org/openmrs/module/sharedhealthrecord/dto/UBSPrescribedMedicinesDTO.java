package org.openmrs.module.sharedhealthrecord.dto;

import org.openmrs.module.sharedhealthrecord.UBSPrescription;

public class UBSPrescribedMedicinesDTO {

	private int pmId;
	
	private String medicineName;
	
	private int medicineId;
	
	private String frequency;
	
	private int duration;
	
	private String instruction;
	
	private UBSPrescriptionDTO prescription;

	public int getPmId() {
		return pmId;
	}

	public void setPmId(int pmId) {
		this.pmId = pmId;
	}

	public String getMedicineName() {
		return medicineName;
	}

	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}

	public int getMedicineId() {
		return medicineId;
	}

	public void setMedicineId(int medicineId) {
		this.medicineId = medicineId;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public UBSPrescriptionDTO getPrescription() {
		return prescription;
	}

	public void setPrescription(UBSPrescriptionDTO prescription) {
		this.prescription = prescription;
	}
}
