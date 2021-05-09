package org.openmrs.module.sharedhealthrecord.api;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRPatientOrigin;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public interface SHRPatientOriginService  extends OpenmrsService {
	
	public SHRPatientOrigin savePatientOrigin (SHRPatientOrigin shrPatientOrigin);
	
	public SHRPatientOrigin getPatientOriginDetailById (String type ,String uuid);
	
	public List<SHRPatientOrigin> getpatientOriginByOriginName (String originName, String actionType);
}
