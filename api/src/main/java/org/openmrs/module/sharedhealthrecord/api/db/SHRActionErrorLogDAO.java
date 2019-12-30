package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;


public interface SHRActionErrorLogDAO {
	public void insertErrorLog(SHRActionErrorLog log);
	public List<SHRActionErrorLog> get_list_by_Action_type(String action_type);
	
	public String delete_by_type_and_id(String action_type,
			String mid);
	
	public void testInsert();
	
	public String delete_by_type_and_uuid(String action_type,String uuid);
	
	public String failedUpdate(String action_type,String uuid);
	
	public String updateSentStatus(int eid,int sent_status);
}
