package org.openmrs.module.sharedhealthrecord.utils;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;

public class NormalPatientSendProcess extends PatientSendProcess {

	@Override
	public int patientSendState(PatientCondition condition) {
		// TODO Auto-generated method stub
		List<SHRExternalPatient> patientsToSend = Context.
				getService(SHRExternalPatientService.class).
					findByPatientUuid(condition.getPatientUuid(),"patient");
		if(patientsToSend.size() == 0)
			return 1;
		if(patientsToSend.get(0).getIs_send_to_central().contains("1"))
			return 2;
		return 0;
	}
	
	
	
	
	

}
