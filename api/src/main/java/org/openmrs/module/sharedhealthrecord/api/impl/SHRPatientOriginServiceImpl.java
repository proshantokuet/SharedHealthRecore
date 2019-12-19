package org.openmrs.module.sharedhealthrecord.api.impl;

import java.util.List;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRPatientOrigin;
import org.openmrs.module.sharedhealthrecord.api.SHRPatientOriginService;
import org.openmrs.module.sharedhealthrecord.api.db.SHRPatientOriginDAO;

public class SHRPatientOriginServiceImpl extends BaseOpenmrsService implements SHRPatientOriginService {
	
	private SHRPatientOriginDAO dao;
	
	public SHRPatientOriginDAO getDao() {
		return dao;
	}

	public void setDao(SHRPatientOriginDAO dao) {
		this.dao = dao;
	}

	@Override
	public SHRPatientOrigin savePatientOrigin(SHRPatientOrigin shrPatientOrigin) {
		// TODO Auto-generated method stub
		return dao.savePatientOrigin(shrPatientOrigin);
	}

	@Override
	public List<SHRPatientOrigin> getpatientOriginByOriginName(
			String originName) {
		// TODO Auto-generated method stub
		return dao.getpatientOriginByOriginName(originName);
	}



}
