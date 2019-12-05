package org.openmrs.module.sharedhealthrecord.api.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.db.SHRActionAuditInfoDAO;
import org.openmrs.module.sharedhealthrecord.api.db.SharedHealthRecordDAO;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;

public class SHRActionAuditInfoServiceImpl extends BaseOpenmrsService  implements SHRActionAuditInfoService {
protected final Log log = LogFactory.getLog(this.getClass());
	
	private SHRActionAuditInfoDAO dao;
	
	/**
     * @param dao the dao to set
     */
    public void setDao(SHRActionAuditInfoDAO dao) {
	    this.dao = dao;
    }
    
    /**
     * @return the dao
     */
    public SHRActionAuditInfoDAO getDao() {
	    return dao;
    }

	@Override
	public List<EventRecordsDTO> getEventRecords(String type) {
		// TODO Auto-generated method stub
		
		return dao.getEventRecords(type);
	}

	@Override
	public List<MoneyReceiptDTO> getMoneyReceipt() {
		// TODO Auto-generated method stub
		return dao.getMoneyReceipt();
	}

	@Override
	public List<MoneyReceiptDTO> getMoneyReceipt(String timestamp) {
		// TODO Auto-generated method stub
		return dao.getMoneyReceipt(timestamp);
	}

	@Override
	public String getLastEntryForPatient() {
		// TODO Auto-generated method stub
		return dao.getLastEntryForPatient();
	}

	@Override
	public String getLastEntryForEncounter() {
		// TODO Auto-generated method stub
		return dao.getLastEntryForEncounter();
	}

	@Override
	public String getLastEntryForMoneyReceipt() {
		// TODO Auto-generated method stub
		return dao.getLastEntryForMoneyReceipt();
	}

	@Override
	public void updateAuditPatient(String last_id) {
		// TODO Auto-generated method stub
		dao.updateAuditPatient(last_id);
		
	}

	@Override
	public void updateAuditEncounter(String last_id) {
		// TODO Auto-generated method stub
		dao.updateAuditEncounter(last_id);
	}

	@Override
	public void updateAuditMoneyReceipt(String last_timestamp) {
		// TODO Auto-generated method stub
		dao.updateAuditMoneyReceipt(last_timestamp);
	}

	@Override
	public String getTimeStampForMoneyReceipt(String mid) {
		// TODO Auto-generated method stub
		return dao.getTimeStampForMoneyReceipt(mid);
	}

	
}
