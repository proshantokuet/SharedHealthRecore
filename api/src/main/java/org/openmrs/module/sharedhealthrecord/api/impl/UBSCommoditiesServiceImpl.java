package org.openmrs.module.sharedhealthrecord.api.impl;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistributeDetails;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistribution;
import org.openmrs.module.sharedhealthrecord.UBSUniqueIdGenerator;
import org.openmrs.module.sharedhealthrecord.api.UBSCommoditiesService;
import org.openmrs.module.sharedhealthrecord.api.db.UBSCommoditiesDAO;

public class UBSCommoditiesServiceImpl extends BaseOpenmrsService implements UBSCommoditiesService {

	private UBSCommoditiesDAO dao;
	
	public UBSCommoditiesDAO getDao() {
		return dao;
	}

	public void setDao(UBSCommoditiesDAO dao) {
		this.dao = dao;
	}


	
	@Override
	public UBSUniqueIdGenerator getLastEntry(String date) {
		// TODO Auto-generated method stub
		return dao.getLastEntry(date);
	}

	@Override
	public UBSUniqueIdGenerator saveOrUpdate(UBSUniqueIdGenerator uniqueId) {
		// TODO Auto-generated method stub
		return dao.saveOrUpdate(uniqueId);
	}

	@Override
	public UBSCommoditiesDistribution saveOrUpdate(
			UBSCommoditiesDistribution ubsCommoditiesDistribution) {
		// TODO Auto-generated method stub
		return dao.saveOrUpdate(ubsCommoditiesDistribution);
	}

	@Override
	public UBSCommoditiesDistribution findByDistributeId(int distributeId) {
		// TODO Auto-generated method stub
		return dao.findByDistributeId(distributeId);
	}

	@Override
	public UBSCommoditiesDistributeDetails findByDistributeDetailsId(
			int distributeDetailsId) {
		// TODO Auto-generated method stub
		return dao.findByDistributeDetailsId(distributeDetailsId);
	}

}
