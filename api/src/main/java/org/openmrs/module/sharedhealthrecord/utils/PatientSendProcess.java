package org.openmrs.module.sharedhealthrecord.utils;

public abstract class PatientSendProcess {
	
	public void sendPatient(PatientSendModel patientInfo){
		if(!getPatientFromLocal(patientInfo)) return;
		
	}
	
	public Boolean getPatientFromLocal(PatientSendModel patientInfo){
		return true;
	}
	
	
}
