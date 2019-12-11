package org.openmrs.module.sharedhealthrecord.web.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.module.sharedhealthrecord.web.controller.rest.SharedHealthRecordManageRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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
	String localServer = "http://192.168.19.145/";
	String centralServer="http://192.168.19.147/";
	
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
	
	public void sendPatient() throws ParseException{
		JSONParser jsonParser = new JSONParser();
		String last_entry = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForPatient();
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
				.getEventRecords("Patient",last_entry);
		///openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full
		
		for(EventRecordsDTO rec: records){
			String patientUUid = rec.getObject().split("/|\\?")[6];
			List<SHRExternalPatient> patientsToSend = Context.
					getService(SHRExternalPatientService.class).
						findByPatientUuid(patientUUid,"patient");
			// If patient is not found in table it must be sent
			if(patientsToSend.size() == 0){				
				patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
				
			}
			else {
				//If patient is found in table with Is_Send_to_Central = 1, it must be sent
				if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
					patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
				}
				else {
					// do nothing
				}
			}
			
		}
	}
	public void sendFailedPatient() throws ParseException{
		List<SHRActionErrorLog> failedPatients = new ArrayList<SHRActionErrorLog>();
		
		for(SHRActionErrorLog failPat : failedPatients){
			Context.getService(SHRActionErrorLogService.class).
			delete_by_type_and_id("Patient", Integer.toString(failPat.getId()));
			patientFetchAndPost(failPat.getUuid(),Integer.toString(failPat.getId()),true);						
		}
		
		
	}
	public void sendEncounter() throws ParseException{
		String last_entry = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForEncounter();
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
				.getEventRecords("Encounter",last_entry);
		
		JSONParser jsonParser = new JSONParser();
		///openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full
		for(EventRecordsDTO rec: records){
			String encounterUUid = rec.getObject().split("/|\\?")[7];
			List<SHRExternalPatient> patientsToSend = Context.
					getService(SHRExternalPatientService.class).
						findByPatientUuid(encounterUUid,"Encounter");
			if(patientsToSend.size() == 0){
				encounterFetchAndPost(encounterUUid,Integer.toString(rec.getId()),false);				
			}
			else {
				if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
					encounterFetchAndPost(encounterUUid,Integer.toString(rec.getId()),false);
				}
				else {
					// do nothing
				}
			}
			
		}
	}
	public void sendFailedEncounter(){
		List<SHRActionErrorLog> failedEncounters = Context.getService(SHRActionErrorLogService.class)
				.get_list_by_Action_type("Encounter");
		
		for(SHRActionErrorLog encounter: failedEncounters){
			String id = Integer.toString(encounter.getId());
			Context.getService(SHRActionErrorLogService.class).
				delete_by_type_and_id("Encounter", id);
			try {
				encounterFetchAndPost(encounter.getUuid(),Integer.toString(encounter.getId()),true);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				SHRActionErrorLog log = new SHRActionErrorLog();
				log.setAction_type("Encounter");
				log.setId(Integer.parseInt(id));
				log.setError_message(e.toString());
				log.setUuid(encounter.getUuid());
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(log);
			}
			
		}
	}
	public void sendMoneyReceipt(){
		JSONParser jsonParser = new JSONParser();
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
				String mid = Integer.toString(receipt.getMid());
				
				MoneyReceiptFetchAndPost(mid,false);
			}
		}catch(Exception e){
			
		}
		// catch will enter the data into shr_action_error_log table
		
		
		
	}
	public void sendFailedMoneyReceipt(){
		List<SHRActionErrorLog> failedReceipts = Context.getService(SHRActionErrorLogService.class)
				.get_list_by_Action_type("Money Receipt");
		
		for(SHRActionErrorLog receipt: failedReceipts){
			String mid = Integer.toString(receipt.getId());
			Context.getService(SHRActionErrorLogService.class).
			delete_by_type_and_id("Money Receipt", mid);
			MoneyReceiptFetchAndPost(mid,true);
			
		}
	}
	
	private void MoneyReceiptFetchAndPost(String mid,Boolean failedReceipt){
		JSONParser jsonParser = new JSONParser();
		try{
			JSONObject jsonMoneyReceipt = new JSONObject();
			String localGetUrl = localServer+"openmrs/ws/rest/v1/money-receipt"
					+ "/get/"+mid;
			String moneyReceipt = HttpUtil.get(localGetUrl,"","admin:test");
			jsonMoneyReceipt = (JSONObject) jsonParser.parse(moneyReceipt);
			JSONObject jsonPostMoneyReceipt = new JSONObject();
			JSONObject jsonNestedPostMoneyReceipt = new JSONObject();
			JSONArray jsonNestedPostServices = new JSONArray();
			JSONArray jsonNestedGetServices = jsonMoneyReceipt.getJSONArray("services");
			//Money Receipt Part
			JSONObject jsonNestedGetMoneyReceipt = jsonMoneyReceipt.getJSONObject("moneyReceipt");
			jsonNestedPostMoneyReceipt.put("clinicName", jsonNestedGetMoneyReceipt.
					get("clinicName"));
			jsonNestedPostMoneyReceipt.put("clinicCode",jsonNestedGetMoneyReceipt.get("clinicCode"));
			jsonNestedPostMoneyReceipt.put("orgUnit",jsonNestedGetMoneyReceipt.get("orgUnit"));
			jsonNestedPostMoneyReceipt.put("slipNo", jsonNestedGetMoneyReceipt.get("slipNo"));
			jsonNestedPostMoneyReceipt.put("reference", jsonNestedGetMoneyReceipt.get("reference"));
			jsonNestedPostMoneyReceipt.put("servicePoint",
					jsonNestedGetMoneyReceipt.get("servicePoint"));
			
			jsonNestedPostMoneyReceipt.put("session",
					jsonNestedGetMoneyReceipt.get("session"));
			
			jsonNestedPostMoneyReceipt.put("other",
					jsonNestedGetMoneyReceipt.get("other"));
			
			jsonNestedPostMoneyReceipt.put("sateliteClinicId",
					jsonNestedGetMoneyReceipt.get("sateliteClinicId"));
			
			jsonNestedPostMoneyReceipt.put("teamNo",
					jsonNestedGetMoneyReceipt.get("teamNo"));
			
			 
			//Data Collector Part
			JSONObject jsonNestedDataCollector = new JSONObject();
			jsonNestedDataCollector.put("id", "");
			jsonNestedDataCollector.put("designation", 
					jsonNestedGetMoneyReceipt.get("designation"));
			jsonNestedDataCollector.put("userRole", "");
			jsonNestedDataCollector.put("username",
					jsonNestedGetMoneyReceipt.get("dataCollector"));
			
			jsonNestedPostMoneyReceipt.put("dataCollector", jsonNestedDataCollector);
			
			//Mid will be remain null
			jsonNestedPostMoneyReceipt.put("mid",
					"");
			
			jsonNestedPostMoneyReceipt.put("patientName", jsonNestedGetMoneyReceipt.get("patientName"));
			jsonNestedPostMoneyReceipt.put("patientUuid", jsonNestedGetMoneyReceipt.get("patientUuid"));
			jsonNestedPostMoneyReceipt.put("uic", jsonNestedGetMoneyReceipt.get("uic"));
			jsonNestedPostMoneyReceipt.put("contact", jsonNestedGetMoneyReceipt.get("contact"));
			jsonNestedPostMoneyReceipt.put("gender", jsonNestedGetMoneyReceipt.get("gender"));
			jsonNestedPostMoneyReceipt.put("dob", jsonNestedGetMoneyReceipt.get("dob"));
			jsonNestedPostMoneyReceipt.put("wealth", jsonNestedGetMoneyReceipt.get("wealth"));
			jsonNestedPostMoneyReceipt.put("isComplete", 1);
			jsonNestedPostMoneyReceipt.put("totalAmount",
					jsonNestedGetMoneyReceipt.get("totalAmount").toString());
			jsonNestedPostMoneyReceipt.put("totalDiscount", 
					jsonNestedGetMoneyReceipt.get("totalDiscount").toString()
					);
			jsonNestedPostMoneyReceipt.put("patientRegisteredDate", 
					jsonNestedGetMoneyReceipt.get("patientRegisteredDate"));
			
			
			jsonPostMoneyReceipt.put("moneyReceipt", jsonNestedPostMoneyReceipt);
			
			//JSON services semi part
			for(int i = 0; i < jsonNestedGetServices.length();i++){
				JSONObject service = jsonNestedGetServices.getJSONObject(i);
				JSONObject servicePost = new JSONObject();
				servicePost.put("discount",service.get("discount"));
				servicePost.put("quantity", service.get("quantity"));
				
				JSONObject code = new JSONObject();
				code.put("code", service.get("code"));
				servicePost.put("code", code);
				
				servicePost.put("unitCost",service.get("unitCost"));
				JSONObject item = new JSONObject();
				item.put("name",service.get("item"));
				item.put("category", service.get("category"));
				
				servicePost.put("item", item);
				
				servicePost.put("category", service.get("category"));
				servicePost.put("totalAmount", service.get("totalAmount").toString());
				servicePost.put("netPayable", service.get("netPayable"));
				
				jsonNestedPostServices.put(servicePost.toString());
			}
			jsonPostMoneyReceipt.put("services", jsonNestedPostServices);
			//JSON Money Receipt Update to Central Server
			String centralPostUrl = centralServer+"openmrs/ws/rest/v1/add-or-update";
			//IF success update timestamp
			String postAction = HttpUtil.post(centralPostUrl, "", jsonPostMoneyReceipt.toString());
			
			if(postAction != null && !"".equalsIgnoreCase(postAction)){
				if(failedReceipt == false){
				String timestamp=Context.getService(SHRActionAuditInfoService.class)
						.getTimeStampForMoneyReceipt(mid);
//				if(!"".equalsIgnoreCase(timestamp))
					String timestampUpdate = 	Context.getService(SHRActionAuditInfoService.class)
				.updateAuditMoneyReceipt(timestamp);
				}
			}
			else {
				//ELSE failed action
				SHRActionErrorLog log = new SHRActionErrorLog();
				log.setAction_type("Money Receipt");
				log.setId(Integer.parseInt(mid));
				log.setError_message(postAction);
				log.setUuid(UUID.randomUUID().toString());
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(log);
			}
		}catch(Exception e){
			SHRActionErrorLog log = new SHRActionErrorLog();
			log.setAction_type("Money Receipt");
			log.setId(Integer.parseInt(mid));
			log.setError_message(e.toString());
			log.setUuid(UUID.randomUUID().toString());
			Context.getService(SHRActionErrorLogService.class)
				.insertErrorLog(log);
		}
	}
	private void patientFetchAndPost(String patientUUid,String id,Boolean failedPatient) throws ParseException{
		
			JSONParser jsonParser = new JSONParser();
		
			// Get Patient Info from Local Server
			String patientUrl = localServer+"openmrs/ws/rest/v1/patient/"+
					patientUUid+"?v=full";
			String patientResponse = HttpUtil.get(patientUrl, "", "admin:test");
			JSONObject getPatient = (JSONObject) jsonParser.parse(patientResponse);
			try{
				
			
				String personUuid = (String) getPatient.get("uuid");
				
				// Model Conversion for Post into Central Server
				org.json.simple.JSONObject getPatient_ = (org.json.simple.JSONObject)
						jsonParser.parse(getPatient.toString());
				
				String postData = SharedHealthRecordManageRestController.
						getPatientObject(getPatient_, personUuid);
				
				//Post to Central Server
				String patientPostUrl = centralServer+
						"/openmrs/ws/rest/v1/bahmnicore/patientprofile";
				
				String returnedResult = HttpUtil.post(patientPostUrl, "", postData);
				
				// Save last entry in Audit Table
				if(failedPatient == false){
					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
						.updateAuditPatient(id);
				}
				
			}catch(Exception e){
				// Error Log Generation on Exception
				SHRActionErrorLog log = new SHRActionErrorLog();
				log.setAction_type("Patient");
				log.setId(Integer.parseInt(id));
				log.setError_message(e.toString());
				log.setUuid(patientUUid);
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(log);
			}
	
	}
	
	private void encounterFetchAndPost(String encounterUuid, String id,Boolean failedEncounter) throws ParseException{
		JSONParser jsonParser = new JSONParser();
			
			
			Boolean status = false;
			;
			try{
				String getUrl = localServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"
						+ encounterUuid + "??includeAll=true";
				String response = HttpUtil.get(getUrl, "", "admin:test");
				JSONObject encounterResponse = (JSONObject) jsonParser.parse(response);
				String postUrl = centralServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
				
				String postResponse = HttpUtil.post(postUrl, "", encounterResponse.toString());
			
				if(failedEncounter == false){
					Context.getService(SHRActionAuditInfoService.class)
						.updateAuditEncounter(id);					
					
				}
			}catch(Exception e){
				SHRActionErrorLog log = new SHRActionErrorLog();
				log.setAction_type("Encounter");
				log.setId(Integer.parseInt(id));
				log.setError_message(e.toString());
				log.setUuid(encounterUuid);
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(log);
			}		
		}

	
	
	
}
