package org.openmrs.module.sharedhealthrecord.api.impl;

import java.util.List;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sharedhealthrecord.api.UBSReportService;
import org.openmrs.module.sharedhealthrecord.api.db.UBSReportDAO;

public class UBSReportServiceImpl extends BaseOpenmrsService implements UBSReportService  {

	private UBSReportDAO dao;
	
	public UBSReportDAO getDao() {
		return dao;
	}

	public void setDao(UBSReportDAO dao) {
		this.dao = dao;
	}
	@Override
	public List<Object[]> getSelectedReport(String startDate, String endDate,
			String reportName) {
		// TODO Auto-generated method stub
		return dao.getSelectedReport(startDate, endDate, reportName);
	}

}
