package org.openmrs.module.sharedhealthrecord.api.impl;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRPatientVisit;
import org.openmrs.module.sharedhealthrecord.api.SHRPatientVisitService;
import org.openmrs.module.sharedhealthrecord.api.db.SHRPatientVisitDAO;

public class SHRPatientVisitServiceImpl extends BaseOpenmrsService implements SHRPatientVisitService {

	private SHRPatientVisitDAO dao;

	public SHRPatientVisitDAO getDao() {
		return dao;
	}

	public void setDao(SHRPatientVisitDAO dao) {
		this.dao = dao;
	}

	@Override
	public SHRPatientVisit savePatientVisit(SHRPatientVisit shrPatientVisit) {
		// TODO Auto-generated method stub
		return dao.savePatientVisit(shrPatientVisit);
	}

	@Override
	public SHRPatientVisit getPatientVisitByVisitUuid(String visitUuid) {
		// TODO Auto-generated method stub
		return dao.getPatientVisitByVisitUuid(visitUuid);
	}
	
}
