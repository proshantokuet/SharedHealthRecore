package org.openmrs.module.sharedhealthrecord.api.db;

import org.openmrs.module.sharedhealthrecord.SHRPatientOrigin;

public interface SHRPatientOriginDAO {

	public SHRPatientOrigin savePatientOrigin (SHRPatientOrigin shrPatientOrigin);
	
	public SHRPatientOrigin getpatientOriginByPatientuuid (String patientUuid);
}
