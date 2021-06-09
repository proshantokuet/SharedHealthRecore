package org.openmrs.module.sharedhealthrecord.dto;

import java.util.Date;
import java.util.Set;

import org.openmrs.module.sharedhealthrecord.UBSPrescribedMedicines;

public class UBSPrescriptionDTO {

	private int prescriptionId;
	
	private String patientUuid;
	
	private String patientName;
	
	private String gender;
	
	private Date visitDate;
	
	private String visitUuid;
	
	private String patientAge;
	
	private String providerName;
	
	private String providerInfo;
	
	private String chiefComplaint;
	
	private String diagnosis;
	
	private String advice;
	
	private Set<UBSPrescribedMedicinesDTO> prescribedMedicine;

	public int getPrescriptionId() {
		return prescriptionId;
	}

	public void setPrescriptionId(int prescriptionId) {
		this.prescriptionId = prescriptionId;
	}

	public String getPatientUuid() {
		return patientUuid;
	}

	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(Date visitDate) {
		this.visitDate = visitDate;
	}

	public String getVisitUuid() {
		return visitUuid;
	}

	public void setVisitUuid(String visitUuid) {
		this.visitUuid = visitUuid;
	}

	public String getPatientAge() {
		return patientAge;
	}

	public void setPatientAge(String patientAge) {
		this.patientAge = patientAge;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getProviderInfo() {
		return providerInfo;
	}

	public void setProviderInfo(String providerInfo) {
		this.providerInfo = providerInfo;
	}

	public String getChiefComplaint() {
		return chiefComplaint;
	}

	public void setChiefComplaint(String chiefComplaint) {
		this.chiefComplaint = chiefComplaint;
	}

	public String getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}

	public String getAdvice() {
		return advice;
	}

	public void setAdvice(String advice) {
		this.advice = advice;
	}

	public Set<UBSPrescribedMedicinesDTO> getPrescribedMedicine() {
		return prescribedMedicine;
	}

	public void setPrescribedMedicine(
			Set<UBSPrescribedMedicinesDTO> prescribedMedicine) {
		this.prescribedMedicine = prescribedMedicine;
	}
}
