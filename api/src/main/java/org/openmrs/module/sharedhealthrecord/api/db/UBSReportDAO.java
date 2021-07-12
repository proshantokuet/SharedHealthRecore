package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

public interface UBSReportDAO {

	List<Object[]> getSelectedReport(String startDate,String endDate,String reportName);
}
