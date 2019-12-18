package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;

import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;

public interface SHRExternalPatientDAO {
	public SHRExternalPatient saveExternalPatient(SHRExternalPatient externalPatient);
	
	public SHRExternalPatient findExternalPatientByPatientUUid (String patientUuid);
	//Find By patientUuid
	public List<SHRExternalPatient> findByPatientUuid(String patientUuid,String type);
}
