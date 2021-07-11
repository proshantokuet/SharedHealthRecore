package org.openmrs.module.sharedhealthrecord.api;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UBSReportService extends OpenmrsService {

	List<Object> getSelectedReport(String startDate,String endDate,String reportName);
}
