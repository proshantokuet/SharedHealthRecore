package org.openmrs.module.sharedhealthrecord.utils;

public class PatientCondition {
	private String patientUuid;
	private int voided;
	private int sent_status;
	
	public String getPatientUuid() {
		return patientUuid;
	}
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	public int getVoided() {
		return voided;
	}
	public void setVoided(int voided) {
		this.voided = voided;
	}
	public int getSent_status() {
		return sent_status;
	}
	public void setSent_status(int sent_status) {
		this.sent_status = sent_status;
	}
	
}
