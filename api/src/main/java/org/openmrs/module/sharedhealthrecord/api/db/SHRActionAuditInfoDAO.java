package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;

public interface SHRActionAuditInfoDAO {
	public List<EventRecordsDTO> getEventRecords(String type);
	public List<MoneyReceiptDTO> getMoneyReceipt();
	public List<MoneyReceiptDTO> getMoneyReceipt(String timestamp);
	
	public String getLastEntryForPatient();
	public String getLastEntryForEncounter();
	public String getLastEntryForMoneyReceipt();
	
	public void updateAuditPatient(String last_id);
	public void updateAuditEncounter(String last_id);
	public void updateAuditMoneyReceipt(String last_timestamp);
	
	public String getTimeStampForMoneyReceipt(String mid);
	
}
