package org.openmrs.module.sharedhealthrecord.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public interface SHRExternalPatientService extends OpenmrsService{
	
	public SHRExternalPatient saveExternalPatient(SHRExternalPatient externalPatient); 
	
	public SHRExternalPatient findExternalPatientByPatientUUid (String patientUuid);
}
