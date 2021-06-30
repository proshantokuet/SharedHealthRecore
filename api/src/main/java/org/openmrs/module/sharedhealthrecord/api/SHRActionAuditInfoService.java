package org.openmrs.module.sharedhealthrecord.api;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SHRActionAuditInfoService extends OpenmrsService{

	public List<EventRecordsDTO> getEventRecords(String type);
	public List<EventRecordsDTO> getEventRecords(String type, String last_entry);
	public List<MoneyReceiptDTO> getMoneyReceipt();
	public List<MoneyReceiptDTO> getMoneyReceipt(String timestamp);
	
	public String getLastEntryForPatient();
	public String getLastEntryForEncounter();
	public String getLastEntryForMoneyReceipt();
	
	
	public String getLastEntryByType(String type);
	
	public String updateAuditInfoByType(String last_id, String type);
	
	public String updateAuditPatient(String last_id);
	public String updateAuditEncounter(String last_id);
	public String updateAuditMoneyReceipt(String last_timestamp);
	
	public String getTimeStampForMoneyReceipt(String mid);
	
	public String getClinicCodeForClinic(String patientUuid);
}
