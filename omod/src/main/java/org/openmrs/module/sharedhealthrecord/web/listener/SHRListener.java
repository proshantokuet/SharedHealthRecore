package org.openmrs.module.sharedhealthrecord.web.listener;

import java.util.List;

import org.json.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;


@Service
@EnableScheduling
@Configuration
@EnableAsync
@Controller
public class SHRListener {
	String localServer = "";
	String globalServer="";
	
	@SuppressWarnings("rawtypes")
	public void sendData() throws Exception {
		JSONObject getResponse = null;
		boolean status = true;
		
		try{
			
		}catch(Exception e){
			status = false;
		}
		
		if(status){
			try{
				sendFailedPatient();
			}catch(Exception e){
				
			}
			try{
				sendPatient();
			}catch(Exception e){
				
			}
			try{
				sendFailedEncounter();
			}catch(Exception e){
				
			}
			try{
				sendEncounter();
			}catch(Exception e){
	
			}
			try{
				sendFailedMoneyReceipt();
			}catch(Exception e){
	
			}
			try{
				sendMoneyReceipt();
			}catch(Exception e){
	
			}
		}
		
	}
	
	public void sendPatient(){
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
				.getEventRecords("Patient");
		///openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full
		for(EventRecordsDTO rec: records){
			String patientUUid = rec.getObject().split("/|\\?")[6];
			List<SHRExternalPatient> patientsToSend = Context.
					getService(SHRExternalPatientService.class).
						findByPatientUuid(patientUUid,"Patient");
			if(patientsToSend.size() == 0){
				// check shr_action_audit_info for last sent id
				// send the patient to central server
				// update shr_action_audit_info
				// try - catch
				// catch will enter the data into shr_action_error_log table
			}
			else {
				if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
					// check shr_action_audit_info for last sent id
					// send the patient to central server
					// update shr_action_audit_info
					// try - catch
					// catch will enter the data into shr_action_error_log table
				}
				else {
					// do nothing
				}
			}
			
		}
	}
	public void sendFailedPatient(){
		
	}
	public void sendEncounter(){
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
				.getEventRecords("Encounter");
		///openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full
		for(EventRecordsDTO rec: records){
			String patientUUid = rec.getObject().split("/|\\?")[6];
			List<SHRExternalPatient> patientsToSend = Context.
					getService(SHRExternalPatientService.class).
						findByPatientUuid(patientUUid,"Encounter");
			if(patientsToSend.size() == 0){
				// check shr_action_audit_info for last sent id
				// send the patient to central server
				// update shr_action_audit_info
				// try - catch
				// catch will enter the data into shr_action_error_log table
			}
			else {
				if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
					// check shr_action_audit_info for last sent id
					// send the patient to central server
					// update shr_action_audit_info
					// try - catch
					// catch will enter the data into shr_action_error_log table
				}
				else {
					// do nothing
				}
			}
			
		}
	}
	public void sendFailedEncounter(){
		
	}
	public void sendMoneyReceipt(){
		// Check shr_action_audit_info for last sent timestamp
		String timestamp = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForMoneyReceipt();
		
		// iterate Money receipt
		try{
			List<MoneyReceiptDTO> receipts = Context.
				getService(SHRActionAuditInfoService.class)
				.getMoneyReceipt(timestamp);
			for(MoneyReceiptDTO receipt: receipts){
				//Local Money Receipt update
				//JSON Money Receipt Update to Central Server
				//IF success update timestamp
				Context.getService(SHRActionAuditInfoService.class)
				.updateAuditMoneyReceipt(timestamp);
				
			}
		}catch(Exception e){
			
		}
		// catch will enter the data into shr_action_error_log table
		
		
		
	}
	public void sendFailedMoneyReceipt(){
		
	}
}
