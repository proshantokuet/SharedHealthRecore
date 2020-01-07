package org.openmrs.module.sharedhealthrecord.web.listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.api.ObservationServiceTest;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;
import org.openmrs.module.sharedhealthrecord.domain.Encounter;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.module.sharedhealthrecord.utils.ServerAddress;
import org.openmrs.module.sharedhealthrecord.web.controller.rest.SharedHealthRecordManageRestController;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@EnableScheduling
@Configuration
@EnableAsync
@Controller
public class SHRListener{
	
	
//	String localServer = "https://192.168.19.145/";
//	String localServer = "https://192.168.19.147/";
//	String localServer = "http://192.168.33.10/";
//	String centralServer="https://192.168.19.147/";
//	String centralServer = "https://192.168.33.10/";
	String localServer = ServerAddress.localServer();
	String centralServer = ServerAddress.centralServer();
	public static DateFormat dateFormatTwentyFourHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@SuppressWarnings("rawtypes")
//	@Scheduled(fixedRate=10000)
	private static final Logger log = LoggerFactory.getLogger(SHRListener.class);
	public void sendAllData() throws Exception {
		
		Context.openSession();
		
		JSONObject getResponse = null;
		boolean status = true;
	
		try{
			
		}catch(Exception e){
			e.printStackTrace();
			status = false;
		}
		
		if(status){
			try{
				sendFailedPatient();
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				sendPatient();

			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				sendFailedEncounter();
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				sendEncounter();
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				sendFailedMoneyReceipt();
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				sendMoneyReceipt();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		Context.closeSession();
		
	}
	
	public void sendPatient() throws ParseException{
		
		String last_entry = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForPatient();
		
		String patUuid = "";
		
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
				.getEventRecords("Patient",last_entry);
						
		try{
			for(EventRecordsDTO rec: records){
					
			String patientUUid = rec.getObject().split("/|\\?")[6];
			patUuid = patientUUid;

			List<SHRExternalPatient> patientsToSend = Context.
					getService(SHRExternalPatientService.class).
						findByPatientUuid(patientUUid,"patient");
			// If patient is not found in  shr_external_patient table it must be sent
			if(patientsToSend.size() == 0){				
				try {
					
					//Init voidedStatus will be 0 in this case
					patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),0);
				} catch (JSONException e) {					
					errorLogInsert("Patient",e.toString(),patientUUid,0);
				}
				
			}
			else {
//				If patient is found in shr_external_patient 
//				table with Is_Send_to_Central = 1, it must be sent
				if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
					try {
						//insert/external_patient will be called to central server (uuid,"1")
						String externalPatientUpdateUrl = centralServer + 
								"openmrs/ws/rest/v1/save-Patient/insert/"
								+ "externalPatient?patient_uuid="
									+patientUUid+"&action_status=1";
						
						String get_result = HttpUtil.get(externalPatientUpdateUrl, "", "admin:test");
//						errorLogUpdate("patient Update to Central Server",get_result,patientUUid);
						
						patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),0);
//						errorLogUpdate("patient","Patient Update/Add Check",patientUUid);
					} catch (JSONException e) {
						errorLogInsert("Patient",e.toString(),patientUUid,0);
						return;
					}
				}
				else {
					// do nothing
				}
			}
			
		}
		}catch(Exception e){
			errorLogInsert("Patient",e.toString(),patUuid,0);
		}
	}
	public void sendFailedPatient() throws ParseException{
		List<SHRActionErrorLog> failedPatients = new ArrayList<SHRActionErrorLog>();
		
		failedPatients = Context.getService(SHRActionErrorLogService.class)
				.get_list_by_Action_type("Patient");
		
		for(SHRActionErrorLog failPat : failedPatients){
			String delResponse = "";
			if(failPat.getVoided() < 2 && failPat.getSent_status() == 0){
				try {
					Boolean flag = patientFetchAndPost(failPat.getUuid(),"",failPat.getVoided()+1);
					Context.getService(SHRActionErrorLogService.class)
					.updateSentStatus(failPat.getEid(), flag == true ? 1 :0);
					
				} catch (JSONException e1) {
					errorLogInsert("Patient",e1.toString(),failPat.getUuid(),failPat.getVoided());
					e1.printStackTrace();
				}
			}
			
		}
		
		
	}
	public void sendEncounter() throws ParseException{
		String last_entry = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForEncounter();
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
				.getEventRecords("Encounter",last_entry);
//		errorLogInsert("Encounter Hits","Encounter Hitting","Testing",0);
		JSONParser jsonParser = new JSONParser();
		for(EventRecordsDTO rec: records){
			String encounterUUid = rec.getObject().split("/|\\?")[7];
			
			//External Patient Table Searching using this encounterUUid
			SHRExternalPatient encounterToSend = Context.
					getService(SHRExternalPatientService.class).findExternalPatientByEncounterUUid(encounterUUid);
			
			//If not found then Send
			if(encounterToSend == null){
				encounterFetchAndPost(encounterUUid,Integer.toString(rec.getId()),0);				
			}
			else {
				//If found and Send_to_central =1 then Send
				if(encounterToSend != null){
					String externalEncounterUpdateUrl = centralServer + 
							"openmrs/ws/rest/v1/save-Patient/insert/"
							+ "externalPatientEncounter?patient_uuid="
								+encounterToSend.getPatient_uuid()+
								"&encounterUuid="+encounterToSend.getEncounter_uuid()+
								"&actionStatus=1";
					
					String get_result = HttpUtil.get(externalEncounterUpdateUrl, "", "admin:test");
//	
					encounterFetchAndPost(encounterUUid,Integer.toString(rec.getId()),0);
					
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
			if(encounter.getVoided() < 2 && encounter.getSent_status() == 0){
				try {
					int val = encounter.getVoided() + 1;
					Boolean flag = encounterFetchAndPost(encounter.getUuid(),"",
							val);	
					Context.getService(SHRActionErrorLogService.class)
						.updateSentStatus(encounter.getEid(), flag == true ? 1 :0);
				} catch (ParseException e) {
					errorLogInsert("Encounter","Encounter Error",encounter.getUuid(),encounter.getVoided()+1);
					e.printStackTrace();
				}
			}
			
			}
			
	}
	
	public void sendMoneyReceipt(){
		JSONParser jsonParser = new JSONParser();
		// Check shr_action_audit_info for last sent timestamp
		String timestamp = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForMoneyReceipt();
		String mid_ = "";
		// iterate Money receipt
		try{
			List<MoneyReceiptDTO> receipts = Context.
				getService(SHRActionAuditInfoService.class)
				.getMoneyReceipt(timestamp);
			for(MoneyReceiptDTO receipt: receipts){
					//Local Money Receipt update
//				
				String mid = Integer.toString(receipt.getMid());
				// 0 is the value of voided Status in case of failure in error log table
				MoneyReceiptFetchAndPost(mid,0);
				mid_ = mid.toString();
			}
		}catch(Exception e){
			errorLogInsert("Money Receipt Error",e.toString(),mid_,0);
		}
		// catch will enter the data into shr_action_error_log table
		
		
		
	}
	public void sendFailedMoneyReceipt(){
		List<SHRActionErrorLog> failedReceipts = Context.getService(SHRActionErrorLogService.class)
				.get_list_by_Action_type("Money Receipt");
//		errorLogUpdate("Money Receipt List","list size check",Integer.toString(failedReceipts.size()));
//		errorLogUpdate("Loop Starts","Loop Starts",UUID.randomUUID().toString());
		for(SHRActionErrorLog receipt: failedReceipts){
			String mid = receipt.getUuid();
			Boolean flag = false;
//			errorLogUpdate("Money Receipt Test","Status:"+receipt.getSent_status(),mid);
			if(receipt.getVoided() < 2 && receipt.getSent_status() == 0){
				// +1 for status incrementing
				
				Boolean sentFlag = MoneyReceiptFetchAndPost(mid,receipt.getVoided() + 1);
				Context.getService(SHRActionErrorLogService.class).
					updateSentStatus(receipt.getEid(), sentFlag == true? 1 : 0);
				
			}
			
		}
	}
	
	private Boolean MoneyReceiptFetchAndPost(String mid,int voidedStatus){
		JSONParser jsonParser = new JSONParser();
//		errorLogUpdate("Money Receript Hitting","Method Hits",mid);
		try{
			JSONObject jsonMoneyReceipt = new JSONObject();
			String localGetUrl = localServer+"openmrs/ws/rest/v1/money-receipt"
					+ "/get/"+mid;
//			errorLogUpdate("Money Receript Get Url",localGetUrl,mid);
			String moneyReceipt = "";
			try{
			 moneyReceipt = HttpUtil.get(localGetUrl,"","admin:test");
			}catch(Exception e){
				
				errorLogInsert("Money Receipt","Money Receipt Get:"+e.toString(),mid,voidedStatus);
				return false;
			}
			
			
			//JSON Money Receipt Update to Central Server
			String postMoneyReceipt = "";
			 try{
				 postMoneyReceipt = moneyReceiptConverter(moneyReceipt);
			 }catch(Exception e){
				 errorLogInsert("Money Receipt",e.toString(),mid,voidedStatus);
				 return false;
			 }
//			errorLogUpdate("Money Receipt Format Post",postMoneyReceipt,mid);
			String centralPostUrl = centralServer+"openmrs/ws/rest/v1/money-receipt/add-or-update";
			//IF success update timestamp
			String postAction = "";
			try{
			 postAction = HttpUtil.post(centralPostUrl, "", postMoneyReceipt);
			}catch(Exception e){
				errorLogInsert("Money Receipt","Money Receipt Post:"+e.toString(),mid,voidedStatus);
				return false;
			}
			
			if(!"".equalsIgnoreCase(postAction)){
				if(voidedStatus == 0){
				String timestamp=Context.getService(SHRActionAuditInfoService.class)
						.getTimeStampForMoneyReceipt(mid);
//				if(!"".equalsIgnoreCase(timestamp))
					String timestampUpdate = 	Context.getService(SHRActionAuditInfoService.class)
				.updateAuditMoneyReceipt(timestamp);
				}
			}
			else {
				errorLogInsert("Money Receipt","Money Receipt Post:"+postAction,mid,voidedStatus);
				return false;
			}
		}catch(Exception e){
			errorLogInsert("Money Receipt",e.toString(),mid,voidedStatus);
			return false;
		}
		
		return true;
	}
	//<param>
		//patientUuid - patient's identity key.
		//id - of event records - for updating last index of a table.
		//failedPatient - flag to check which kind of encounter it is.
	//</param>
	private Boolean patientFetchAndPost(String patientUUid,String id,int voidedStatus) throws ParseException, JSONException{
		
			JSONParser jsonParser = new JSONParser();
		
			// Get Patient Info from Local Server
			String patientUrl = localServer+"openmrs/ws/rest/v1/patient/"+
					patientUUid+"?v=full";

			String patientResponse = "";
			try{
				patientResponse = HttpUtil.get(patientUrl, "", "admin:test");								
			}catch(Exception e){
				errorLogInsert("Patient","Patient Get Error"+e.toString(),patientUUid,voidedStatus);
				return false;
			}

			JSONObject getPatient = new JSONObject(patientResponse);
			try{
				
			
				String personUuid = (String) getPatient.get("uuid");
				
				// Model Conversion for Post into Central Server
				org.json.simple.JSONObject getPatient_ = (org.json.simple.JSONObject)
						jsonParser.parse(getPatient.toString());
				
				String postData = SharedHealthRecordManageRestController.
						getPatientObject(getPatient_, personUuid);
//				errorLogUpdate("Patient Post Format Data",postData,patientUUid);
				//Post to Central Server

				String patientPostUrl = centralServer+
						"openmrs/ws/rest/v1/bahmnicore/patientprofile";
				//Finding Add or Update action 
				//If add to central Server then previous patientPostUrl is Ok
				//Else if update to central Server than previous patientPost += uuid
				
				//Add or Update ? which Action to Find 
				//By checking exist in central server or not
				
				//Central Server patientUuid Check
				String centralServerPatientCheckUrl = centralServer+"openmrs/ws/rest/v1/patient/"+
						patientUUid+"?v=full";
				String centralServerPatientCheckResponse = "";
				try{
					centralServerPatientCheckResponse = HttpUtil.get(centralServerPatientCheckUrl,
								"", "admin:test");
				}catch(Exception e){
					errorLogInsert("Patient","Server Patient Check: "+ e.toString(),patientUUid,voidedStatus);
					return false;
				}
				JSONObject patienResponseCheck = new JSONObject(centralServerPatientCheckResponse);
				
				//If Error No String concat as Add Action - Else Update action API
				patientPostUrl += patienResponseCheck.has("error") ? "" : "/"+patientUUid;
				String returnedResult = "";
								
				try{ 
					
					returnedResult = HttpUtil.post(patientPostUrl, "", postData);
//					errorLogUpdate("patient post",returnedResult,patientUUid);
					//origin table will be inserted in global server for addition only
					if(patienResponseCheck.has("error")){
						String insertUrl = centralServer+"openmrs/ws/rest/v1/save-Patient/insert/patientOriginDetails";
							insertUrl += "?patient_uuid="+patientUUid+"&patient_origin="+localServer;
							
						String get = "";
						try{
							get = HttpUtil.get(insertUrl, "", "admin:test");
						}catch(Exception e){
							errorLogInsert("Patient","Local Server Save Info Error:" + e.toString(),patientUUid,voidedStatus);
							return false;
						}
					}
				
				}catch(Exception e){
					errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus);
					return false;
				}
				// Save last entry in Audit Table
				if(voidedStatus == 0){
					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
						.updateAuditPatient(id);
				}
				
			}catch(Exception e){
				// Error Log Generation on Exception
				errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus);
			}
			
			return true;
	
	}
	//<param>
	//encounterUuid - encounter's identity key.
	//id - of event records - for updating last index of a table.
	//int voidedStatus - voidStatus : 0 failed Once encounter, 1 means failed Twice
	//</param>
	private Boolean encounterFetchAndPost(String encounterUuid, String id,int voidedStatus) throws ParseException{
		JSONParser jsonParser = new JSONParser();
		Boolean visitFlagError = false;
			
			try{
				//Get Encounter Info From Local Server
				String getUrl = localServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"
						+ encounterUuid + "?includeAll=true";
				String response = "";
				try{
					response = HttpUtil.get(getUrl, "", "admin:test");					
				}catch(Exception e){
					errorLogInsert("Encounter","Encounter get Error:"+response,encounterUuid,voidedStatus);
					return false;
				}
				
				//Encounter Response Formatting
				JSONObject encounterResponse = new JSONObject(response);
				org.json.simple.JSONObject enc_response = new org.json.simple.JSONObject();
				try{
				 enc_response = (org.json.simple.JSONObject) jsonParser.
						parse(encounterResponse.toString());
				}catch(Exception e){
					errorLogInsert("Encounter",e.toString(),encounterUuid,voidedStatus);
					return false;
				}
				
				//Fetching visit from Encounter
				String visitUuid = enc_response.get("visitUuid").toString();
				String visitFetchUrl = "";
				String vis_global_response = "";
				
				//Central Server Visit Existence Check
				try{
				
					visitFetchUrl = centralServer+
							"openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid="+visitUuid;
					vis_global_response = HttpUtil.get(visitFetchUrl, "","admin:test");
					
				}catch(Exception e){
					errorLogInsert("Encounter","Encounter Search Error"+e.toString(),encounterUuid,voidedStatus);
					return false;
				}
				
				//Visit Response Parsing
				JSONParser jsonParser1 = new JSONParser();
				org.json.simple.JSONObject visitFetchJsonObj = (org.json.simple.JSONObject) 
						jsonParser1.parse(vis_global_response);
				
				String vis_response = "";
				//IF Not exist proceed to create Visit on Central Server	
				if(visitFetchJsonObj.get("isFound").toString().contains("false")){
					//Local Server Visit Fetch
					String vis_url =  localServer+
							"openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid="+visitUuid;
					try{ 
					vis_response = HttpUtil.get(vis_url, "", "admin:test");				
					}catch(Exception e){
						errorLogInsert("Encounter","Encounter Visit Response:"+e.toString()
								,encounterUuid,voidedStatus);
						return false;
					}
					
					org.json.simple.JSONObject visit_response = new org.json.simple.JSONObject();
					//Visit Response Parsing
					try{
					 visit_response = (org.json.simple.JSONObject) jsonParser.parse(vis_response);
					}catch(Exception e){
						errorLogInsert("Encounter","Encounter Visit Json Parse Error:"+e.toString(),
								encounterUuid,voidedStatus);
						return false;
					}
					String createVisit_ = "";
					//Create Visit in Central Server
					try{ 
						createVisit_ = createVisit(visit_response,enc_response.get("patientUuid").toString());
						JSONObject createVisitResponse = new JSONObject(createVisit_);
						visitFlagError = createVisitResponse.get("isSuccessfull").toString().contains("true")
											? false: true;
					}catch(Exception e){
						errorLogInsert("Encounter","Create Visit Error:"+e.toString(),
								encounterUuid,voidedStatus);
						return false;
					}
					
				}
				// If exist on central Server do nothing
				else if(visitFetchJsonObj.get("isFound").toString().contains("true")){
					// do nothing		
				}
				// Create Visit Exception will stop the proceeding process
				if(visitFlagError == true) {
					errorLogInsert("Encounter","Encounter Visit Creation Error",
							encounterUuid,voidedStatus);
					return false;
				}
				

				// Encounter Post JSON Format Preparation
				String visitTypeValue =SharedHealthRecordManageRestController.visitTypeMapping.get(enc_response.get("visitTypeUuid").toString());
				enc_response.remove("visitTypeUuid");
				enc_response.put("visitType", visitTypeValue);
								
				if(enc_response.containsKey("locationUuid"))
				{
					enc_response.remove("locationUuid");
				}
				if(enc_response.containsKey("location"))
					enc_response.remove("location");

				enc_response.put("location", "8d6c993e-c2cc-11de-8d13-0010c6dffd0f");
				
				//Encounter Post
				String postUrl = centralServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
				
				//Finding Add or Update Action for Post Url
				
				org.json.simple.JSONArray obs = SharedHealthRecordManageRestController.getObservations((org.json.simple.JSONArray)enc_response.get("observations"));
//				enc_response.remove("observations");
				org.json.simple.JSONObject encounter = (org.json.simple.JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(enc_response.toString(),Encounter.class)));
				encounter.put("observations", obs);

				//Encounter  Existence Check in Global Server
				String searchEncounterUrl = centralServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"
						+ encounterUuid + "?includeAll=true";
				String globalEncounterResponse = "";
				try{
					globalEncounterResponse = HttpUtil.get(searchEncounterUrl, "", "admin:test");
				}catch(Exception e){
					errorLogInsert("Encounter","Encounter Global Search error:"+e.toString(),
							encounterUuid,voidedStatus);
					return false;
				}
				
				//Json Parsing
				JSONObject globalSearchEncounter = new JSONObject(globalEncounterResponse);
				
				
				//If found on Global Server then delete Encounter
				if(globalSearchEncounter.has("encounterUuid")){
					deleteEncounter(encounterUuid);
				}
				
				
				//Post Encounter to Global Server
				try{
					String postResponse = HttpUtil.post(postUrl, "", encounter.toJSONString());
//					errorLogUpdate("Encounter Post Final",postResponse,encounterUuid);
				}catch(Exception e){
					errorLogInsert("Encounter","Encounter post error:"+e.toString(),
							encounterUuid,voidedStatus);
					return false;
				}
				//voided Status Increment
				if(voidedStatus == 0){					
					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
					.updateAuditEncounter(id);					
				}
			}catch(Exception e){
				errorLogInsert("Encouner","Encounter Error:"+e.toString(),encounterUuid,voidedStatus);
				return false;
			}
			
			return true;
		}
	
	//For creating visit in encounter sending
	private String createVisit(org.json.simple.JSONObject obj,String patientUuid){
		String visitSavingResponse = "";
		obj.remove("isFound");
		obj.put("patient_uuid", patientUuid);
		try {
			
			String visitSavingUrl = centralServer + "openmrs/ws/rest/v1/save-Patient/insert/patientVisitDetails";			
			visitSavingResponse = HttpUtil.post(visitSavingUrl, "", obj.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return visitSavingResponse;
	}

	public void errorLogUpdate(String type,String message, String uuId){
		Context.clearSession();
		Context.openSession();
		SHRActionErrorLog log = new SHRActionErrorLog();
		log.setAction_type(type);
		log.setError_message(message);
		log.setUuid(uuId);
		log.setVoided(0);
		Context.getService(SHRActionErrorLogService.class)
			.insertErrorLog(log);
		Context.clearSession();
		Context.openSession();
	}
	
	public void errorLogInsert(String action_type,String message,String uuId,Integer voided){
		Context.clearSession();
		Context.openSession();
		//Delete existing if void > 0
		if(voided > 0) {
			Context.getService(SHRActionErrorLogService.class)
				.delete_by_type_and_uuid(action_type, uuId);
		}
		//Insert Log
		SHRActionErrorLog log = new SHRActionErrorLog();
		log.setAction_type(action_type);
		log.setError_message(message);
		log.setUuid(uuId);
		log.setVoided(voided);
		//Insert will be called on exception 
		//So 0 - will be inserted automatically
		log.setSent_status(0);
		Context.getService(SHRActionErrorLogService.class)
			.insertErrorLog(log);
		Context.clearSession();
		Context.openSession();
	}
	

	public void errorLogMoneyReceipt(String type,String message,String mid){
		Context.clearSession();
		Context.openSession();
		SHRActionErrorLog log = new SHRActionErrorLog();
		log.setAction_type(type);
		log.setError_message(message);
		log.setId(Integer.parseInt(mid));
		log.setUuid(UUID.randomUUID().toString());
		Context.getService(SHRActionErrorLogService.class)
			.insertErrorLog(log);
		Context.clearSession();
		Context.openSession();
	}
	
	//Money Receipt Get to Post JSON Converter
	private String moneyReceiptConverter(String moneyReceipt) throws JSONException{
		String moneyReceiptPost = "";
		JSONObject jsonMoneyReceipt = new JSONObject(moneyReceipt);
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
		jsonNestedPostMoneyReceipt.put("moneyReceiptDate",
				jsonNestedGetMoneyReceipt.get("moneyReceiptDate") );
		
		 
		//Data Collector Part
		JSONObject jsonNestedDataCollector = new JSONObject();
		jsonNestedDataCollector.put("id", "");
		jsonNestedDataCollector.put("designation", 
				jsonNestedGetMoneyReceipt.get("designation"));
		jsonNestedDataCollector.put("userRole", "");
		jsonNestedDataCollector.put("username",
				jsonNestedGetMoneyReceipt.get("dataCollector"));
		
		jsonNestedPostMoneyReceipt.put("dataCollector", jsonNestedDataCollector);
		
		//Mid will remain null
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
			
			jsonNestedPostServices.put(servicePost);
		}
		jsonPostMoneyReceipt.put("services", jsonNestedPostServices);
		return jsonPostMoneyReceipt.toString();
	}
	
	
	private void deleteEncounter(String encounterUuid){
		String deleteWithoutPurge = 
				centralServer+"openmrs/ws/rest/v1/encounter/"+encounterUuid;
		String deleteFirst = HttpUtil.delete(deleteWithoutPurge, "", "admin:test");
		//delete encounter
//		errorLogInsert("Error Log Delete ",deleteFirst,encounterUuid,0);
		String deleteUrlString = 
				centralServer+"openmrs/ws/rest/v1/encounter/"+encounterUuid
		+"?purge=true";
		String result = HttpUtil.delete(deleteUrlString, "", "admin:test");
	}
	
	
}
