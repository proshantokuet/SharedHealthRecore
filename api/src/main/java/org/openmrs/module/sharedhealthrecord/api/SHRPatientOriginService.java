package org.openmrs.module.sharedhealthrecord.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRPatientOrigin;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public interface SHRPatientOriginService  extends OpenmrsService {
	
	public SHRPatientOrigin savePatientOrigin (SHRPatientOrigin shrPatientOrigin);
	
	public SHRPatientOrigin getpatientOriginByPatientuuid (String patientUuid);
}
