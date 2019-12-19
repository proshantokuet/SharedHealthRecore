package org.openmrs.module.sharedhealthrecord.api.db;

import org.openmrs.module.sharedhealthrecord.SHRPatientVisit;

public interface SHRPatientVisitDAO {
	
	public SHRPatientVisit savePatientVisit (SHRPatientVisit shrPatientVisit);
	
	public SHRPatientVisit getPatientVisitByVisitUuid (String visitUuid);
	
	public SHRPatientVisit getPatientIdByPatientUuid (String patientUuid);
}
