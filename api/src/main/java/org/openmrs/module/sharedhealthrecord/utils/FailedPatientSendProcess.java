package org.openmrs.module.sharedhealthrecord.utils;

public class FailedPatientSendProcess extends PatientSendProcess{

	

	@Override
	public int patientSendState(PatientCondition condition) {
		// TODO Auto-generated method stub
		return 
				(condition.getVoided() < 2 && condition.getSent_status() == 0)
					== true ? 3 :0;
		//3 for failed Patiend Sending
	}

	
	
}
