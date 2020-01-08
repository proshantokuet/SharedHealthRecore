package org.openmrs.module.sharedhealthrecord.utils;

public class PatientSendModel {
	private String patientUuid;
	private String localServer;
	private String centralServer;
	private String event_records_id;
	private int voidedStatus;
	private int eid;
	
	public int getEid() {
		return eid;
	}
	public void setEid(int eid) {
		this.eid = eid;
	}
	public String getPatientUuid() {
		return patientUuid;
	}
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	public String getLocalServer() {
		return localServer;
	}
	public void setLocalServer(String localServer) {
		this.localServer = localServer;
	}
	public String getCentralServer() {
		return centralServer;
	}
	public void setCentralServer(String centralServer) {
		this.centralServer = centralServer;
	}
	public String getEvent_records_id() {
		return event_records_id;
	}
	public void setEvent_records_id(String event_records_id) {
		this.event_records_id = event_records_id;
	}
	public int getVoidedStatus() {
		return voidedStatus;
	}
	public void setVoidedStatus(int voidedStatus) {
		this.voidedStatus = voidedStatus;
	}
	
}
