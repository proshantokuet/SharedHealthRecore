package org.openmrs.module.sharedhealthrecord.api.impl;

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
		
	}
}
