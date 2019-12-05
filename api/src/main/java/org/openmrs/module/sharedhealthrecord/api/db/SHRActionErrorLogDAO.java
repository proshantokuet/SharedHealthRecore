package org.openmrs.module.sharedhealthrecord.api.db;

import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;


public interface SHRActionErrorLogDAO {
	public void insertErrorLog(SHRActionErrorLog log);
}
