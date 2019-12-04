package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;

public interface SHRActionAuditInfoDAO {
	List<EventRecordsDTO> getEventRecords(String type);
}
