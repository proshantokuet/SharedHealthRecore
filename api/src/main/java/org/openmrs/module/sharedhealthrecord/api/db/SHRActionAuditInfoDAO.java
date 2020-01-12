package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;

public interface SHRActionAuditInfoDAO {
	public List<EventRecordsDTO> getEventRecords(String type);
	public List<EventRecordsDTO> getEventRecords(String type,String last_entry);
	public List<MoneyReceiptDTO> getMoneyReceipt();
	public List<MoneyReceiptDTO> getMoneyReceipt(String timestamp);
	
	public String getLastEntryForPatient();
	public String getLastEntryForEncounter();
	public String getLastEntryForMoneyReceipt();
	
	public String updateAuditPatient(String last_id);
	public String updateAuditEncounter(String last_id);
	public String updateAuditMoneyReceipt(String last_timestamp);
	
	public String getTimeStampForMoneyReceipt(String mid);
	
	
}
