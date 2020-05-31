package org.openmrs.module.sharedhealthrecord.domain;

public class MoneyReceiptDTO {
	private int mid;
	private String patient_uuid;
	private String timestamp;
	private String eslipNo;
	public int getMid() {
		return mid;
	}
	public void setMid(int mid) {
		this.mid = mid;
	}
	public String getPatient_uuid() {
		return patient_uuid;
	}
	public void setPatient_uuid(String patient_uuid) {
		this.patient_uuid = patient_uuid;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getEslipNo() {
		return eslipNo;
	}
	public void setEslipNo(String eslipNo) {
		this.eslipNo = eslipNo;
	}
	
	
	
}
