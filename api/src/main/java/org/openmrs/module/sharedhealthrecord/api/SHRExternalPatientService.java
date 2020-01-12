package org.openmrs.module.sharedhealthrecord.api;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public interface SHRExternalPatientService extends OpenmrsService{
	public List<SHRExternalPatient> findByPatientUuid(String patientUuid,String type);
	public SHRExternalPatient saveExternalPatient(
			SHRExternalPatient externalPatient);
	SHRExternalPatient findExternalPatientByPatientUUid(String patientUuid);
	
	
	public SHRExternalPatient findExternalPatientByEncounterUUid (String encounterUuid);
}
