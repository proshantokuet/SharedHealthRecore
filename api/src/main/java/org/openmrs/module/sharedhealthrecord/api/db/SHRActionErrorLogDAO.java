package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;


public interface SHRActionErrorLogDAO {
	public void insertErrorLog(SHRActionErrorLog log);
	public List<SHRActionErrorLog> get_list_by_Action_type(String action_type);
}
