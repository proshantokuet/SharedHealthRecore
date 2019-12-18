package org.openmrs.module.sharedhealthrecord.api;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.springframework.transaction.annotation.Transactional;
@Transactional

public interface SHRActionErrorLogService extends OpenmrsService{
	public void insertErrorLog(SHRActionErrorLog log);
	
	public List<SHRActionErrorLog> get_list_by_Action_type(String action_type);
	
	public String delete_by_type_and_id(String action_type,String mid);
	
	public void testInsert();
}
