package org.openmrs.module.sharedhealthrecord.api.impl;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;
import org.openmrs.module.sharedhealthrecord.api.db.SHRExternalPatientDAO;

public class SHRExternalPatientServiceImpl extends BaseOpenmrsService implements SHRExternalPatientService {
	
	private SHRExternalPatientDAO dao;
	public SHRExternalPatientDAO getDao() {
		return dao;
	}
	public void setDao(SHRExternalPatientDAO dao) {
		this.dao = dao;
	}
	@Override
	public SHRExternalPatient saveExternalPatient(SHRExternalPatient externalPatient) {
		// TODO Auto-generated method stub
		return dao.saveExternalPatient(externalPatient);
	}
	
}
