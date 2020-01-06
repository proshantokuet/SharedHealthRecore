package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

import org.openmrs.module.sharedhealthrecord.SHRPatientOrigin;

public interface SHRPatientOriginDAO {

	public SHRPatientOrigin savePatientOrigin (SHRPatientOrigin shrPatientOrigin);
	
	public List<SHRPatientOrigin> getpatientOriginByOriginName (String originName, String actionType);
}
