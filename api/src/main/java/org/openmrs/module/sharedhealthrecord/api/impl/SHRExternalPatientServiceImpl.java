package org.openmrs.module.sharedhealthrecord.api.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;
import org.openmrs.module.sharedhealthrecord.api.db.SHRExternalPatientDAO;

public class SHRExternalPatientServiceImpl extends BaseOpenmrsService implements SHRExternalPatientService {
	protected final Log log = LogFactory.getLog(this.getClass());
	private SHRExternalPatientDAO dao;
	public SHRExternalPatientDAO getDao() {
		return dao;
	}
	public void setDao(SHRExternalPatientDAO dao) {
		this.dao = dao;
	}
	@Override
	public List<SHRExternalPatient> findByPatientUuid(String patientUuid,
			String type) {
		// TODO Auto-generated method stub
		return dao.findByPatientUuid(patientUuid, type);
	}
	

	
	
}
