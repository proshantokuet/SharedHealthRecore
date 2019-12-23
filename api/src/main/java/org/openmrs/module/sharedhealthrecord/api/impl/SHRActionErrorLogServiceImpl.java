package org.openmrs.module.sharedhealthrecord.api.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.api.db.SHRActionErrorLogDAO;
import org.openmrs.module.sharedhealthrecord.api.db.SharedHealthRecordDAO;

public class SHRActionErrorLogServiceImpl extends BaseOpenmrsService implements SHRActionErrorLogService {
protected final Log log = LogFactory.getLog(this.getClass());
	
	private SHRActionErrorLogDAO dao;
	
	/**
     * @param dao the dao to set
     */
    public void setDao(SHRActionErrorLogDAO dao) {
	    this.dao = dao;
    }
    
    /**
     * @return the dao
     */
    public SHRActionErrorLogDAO getDao() {
	    return dao;
    }

	@Override
	public void insertErrorLog(SHRActionErrorLog log) {
		// TODO Auto-generated method stub
		dao.insertErrorLog(log);
		
	}

	@Override
	public List<SHRActionErrorLog> get_list_by_Action_type(String action_type) {
		// TODO Auto-generated method stub
		return dao.get_list_by_Action_type(action_type);
	}

	@Override
	public String delete_by_type_and_id(String action_type,
			String mid) {
		// TODO Auto-generated method stub
		return dao.delete_by_type_and_id(action_type, mid);
	}

	@Override
	public void testInsert() {
		// TODO Auto-generated method stub
		dao.testInsert();
		
	}
}
