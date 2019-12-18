package org.openmrs.module.sharedhealthrecord.api;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRPatientVisit;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public interface SHRPatientVisitService extends OpenmrsService {
	
	public SHRPatientVisit savePatientVisit (SHRPatientVisit shrPatientVisit);
	
	public SHRPatientVisit getPatientVisitByVisitUuid (String visitUuid);
}
