package org.openmrs.module.sharedhealthrecord.api.db;

import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;

public interface SHRExternalPatientDAO {
	public SHRExternalPatient saveExternalPatient(SHRExternalPatient externalPatient);
	
	public SHRExternalPatient findExternalPatientByPatientUUid (String patientUuid);
}
