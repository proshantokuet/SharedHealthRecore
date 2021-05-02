package org.openmrs.module.sharedhealthrecord.web.listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;
import org.openmrs.module.sharedhealthrecord.domain.Encounter;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.module.sharedhealthrecord.utils.ServerAddress;
import org.openmrs.module.sharedhealthrecord.web.controller.rest.SharedHealthRecordManageRestController;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
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
public class SHRListenerFailedEncounter{
	
	String localServer = ServerAddress.localServer();
	String centralServer = ServerAddress.centralServer();
	String isDeployInGlobal = ServerAddress.isDeployInGlobal;
	public static DateFormat dateFormatTwentyFourHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final ReentrantLock lock = new ReentrantLock();
	
	@SuppressWarnings("rawtypes")
//	@Scheduled(fixedRate=10000)
	private static final Logger log = LoggerFactory.getLogger(SHRListenerFailedEncounter.class);
	public void sendAllData() throws Exception {
		if (!lock.tryLock()) {
			log.error("It is already in progress.");
	        return;
		}
		log.error("isDeployInGlobal " + isDeployInGlobal);
		if(isDeployInGlobal.equalsIgnoreCase("0")) {
			Context.openSession();
			
			JSONObject getResponse = null;
			boolean status = true;
			try{
				String globalServerUrl = centralServer + "openmrs/ws/rest/v1/visittype";
				String get_result = HttpUtil.get(globalServerUrl, "", "admin:test");
				JSONObject patienResponseCheck = new JSONObject(get_result);			
			}catch(Exception e){
				e.printStackTrace();
				status = false;
			}
			
			if(status){
				try{
					sendFailedEncounter();
				}catch(Exception e){
					e.printStackTrace();
				}
				finally {
					lock.unlock();
					log.error("complete listener encounter failed at:" +new Date());
				}

			}
			
			Context.closeSession();
		}
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
					Boolean saveFlagPatient = patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),0);
					if(saveFlagPatient) {
						SaveStatusOfEachOnSync("Patient", "success", patientUUid);						
					}
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
						
						
//						errorLogUpdate("patient Update to Central Server",get_result,patientUUid);
						
						Boolean saveFlagPatient = patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),0);
						if(saveFlagPatient) {
							SaveStatusOfEachOnSync("Patient", "success", patientUUid);
							String get_result = HttpUtil.get(externalPatientUpdateUrl, "", "admin:test");						
						}
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
			String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
					.updateAuditPatient(Integer.toString(rec.getId()));
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
			boolean status = true;
			try{
				String globalServerUrl = centralServer + "openmrs/ws/rest/v1/visittype";
				String get_result = HttpUtil.get(globalServerUrl, "", "admin:test"); 
				
			}catch(Exception e){
				e.printStackTrace();
				status = false;
			}
			if(status) {
				if(failPat.getSent_status() == 0){
					try {
						int val = failPat.getVoided()+1;
						Boolean flag = patientFetchAndPost(failPat.getUuid(),"",val > 1 ? 2 : val);
						Context.getService(SHRActionErrorLogService.class)
						.updateSentStatus(failPat.getEid(), flag == true ? 1 :0);
						if(flag) {
							List<SHRExternalPatient> patientsToSend = Context.
									getService(SHRExternalPatientService.class).
										findByPatientUuid(failPat.getUuid(),"patient");
							if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
									//insert/external_patient will be called to central server (uuid,"1")
									String externalPatientUpdateUrl = centralServer + 
											"openmrs/ws/rest/v1/save-Patient/insert/"
											+ "externalPatient?patient_uuid="
												+failPat.getUuid()+"&action_status=1";
									String get_result = HttpUtil.get(externalPatientUpdateUrl, "", "admin:test");
							}
						}
					} catch (JSONException e1) {
						errorLogInsert("Patient",e1.toString(),failPat.getUuid(),failPat.getVoided());
						e1.printStackTrace();
					}
				}
			}
		}
	}
	public void sendEncounter() throws ParseException{
		JSONParser jsonParser = new JSONParser();
		String last_entry = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForEncounter();
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
				.getEventRecords("Encounter",last_entry);

			for(EventRecordsDTO rec: records){
				try {
					String encounterUUid = rec.getObject().split("/|\\?")[7];
					String getEncounterUrl = localServer +"openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"+ encounterUUid +"?includeAll=false";
					String patientencounterResponse = HttpUtil.get(getEncounterUrl, "", "admin:test");
					org.json.simple.JSONObject obj = (org.json.simple.JSONObject) jsonParser.parse(patientencounterResponse);
					String patientUuid = (String) obj.get("patientUuid");
					List<SHRExternalPatient> patientsToSend = Context.getService(SHRExternalPatientService.class).
								findByPatientUuid(patientUuid,"patient");
					SHRExternalPatient encounterToSend = Context.
							getService(SHRExternalPatientService.class).
								findExternalPatientByEncounterUUid(encounterUUid);
//					log.error("encounter fetch: "+encounterToSend != null ? encounterToSend.toString():"Null");
					//If not found then Send
					if(encounterToSend == null && patientsToSend.size() == 0){
						Boolean EncounterSaveFlag = encounterFetchAndPost(encounterUUid,Integer.toString(rec.getId()),0);
						if(EncounterSaveFlag) {
							SaveStatusOfEachOnSync("Encounter", "success", encounterUUid);
						}
					}
					else {
						//If found and Send_to_central =1 then Send
						if(encounterToSend != null){
							String externalEncounterUpdateUrl = centralServer + 
									"openmrs/ws/rest/v1/save-Patient/insert/"
									+ "globalExternalPatientEncounter?patient_uuid="
										+encounterToSend.getPatient_uuid()+
										"&encounterUuid="+encounterToSend.getEncounter_uuid()+
										"&actionStatus=1";

							Boolean EncounterSaveFlag =  encounterFetchAndPost(encounterUUid,Integer.toString(rec.getId()),0);
							if(EncounterSaveFlag) {
								SaveStatusOfEachOnSync("Encounter", "success", encounterUUid);
								String get_result = HttpUtil.get(externalEncounterUpdateUrl, "", "admin:test");		
							}			
						}
					}
					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
					.updateAuditEncounter(Integer.toString(rec.getId()));	
				} catch (Exception e) {
					String encounterUUidEx = rec.getObject().split("/|\\?")[7];
					errorLogInsert("Encounter",e.toString(),encounterUUidEx,0);
				}
			}
	}
	
	public void sendFailedEncounter(){
		List<SHRActionErrorLog> failedEncounters = Context.getService(SHRActionErrorLogService.class)
				.get_list_by_Action_type("Encounter");
		for(SHRActionErrorLog encounter: failedEncounters){
			boolean status = true;
			try{
				String globalServerUrl = centralServer + "openmrs/ws/rest/v1/visittype";
				String get_result = HttpUtil.get(globalServerUrl, "", "admin:test"); 
				
			}catch(Exception e){
				e.printStackTrace();
				status = false;
			}
			if(status) {
				if(encounter.getSent_status() == 0){
					try {
						int val = encounter.getVoided() + 1;
						Boolean flag = encounterFetchAndPost(encounter.getUuid(),"",
								val > 1 ? 2 : val);	
						Context.getService(SHRActionErrorLogService.class)
							.updateSentStatus(encounter.getEid(), flag == true ? 1 :0);
						if(flag) {
							SHRExternalPatient encounterToSend = Context.
									getService(SHRExternalPatientService.class).
										findExternalPatientByEncounterUUid(encounter.getUuid());
							if(encounterToSend != null){
								String externalEncounterUpdateUrl = centralServer + 
										"openmrs/ws/rest/v1/save-Patient/insert/"
										+ "globalExternalPatientEncounter?patient_uuid="
											+encounterToSend.getPatient_uuid()+
											"&encounterUuid="+encounterToSend.getEncounter_uuid()+
											"&actionStatus=1";
								String get_result = HttpUtil.get(externalEncounterUpdateUrl, "", "admin:test");
							}
						}
					} catch (ParseException e) {
						if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
							errorLogInsert("Encounter","Encounter Error",encounter.getUuid(),encounter.getVoided() == 2 ? 1 : encounter.getVoided());
						}
						else {
							errorLogInsert("Encounter","Encounter Error",encounter.getUuid(),encounter.getVoided());
						}
						e.printStackTrace();
					}
				}
			}
			
		}
	}
	
	public void sendMoneyReceipt(){
		JSONParser jsonParser = new JSONParser();
		// Check shr_action_audit_info for last sent timestamp
		String timestamp = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForMoneyReceipt();
		List<MoneyReceiptDTO> receipts = Context.
				getService(SHRActionAuditInfoService.class)
				.getMoneyReceipt(timestamp);
			for(MoneyReceiptDTO receipt: receipts){
				try{
//					String globalServerUrl = centralServer+"openmrs/ws/rest/v1/money-receipt"
//							+ "/geteslip/"+receipt.getEslipNo();
//					String moneyReceiptByEslip = "";
//					moneyReceiptByEslip = HttpUtil.get(globalServerUrl,"","admin:test");
//					JSONObject jsonMoneyReceipt = new JSONObject(moneyReceiptByEslip);
//					if(jsonMoneyReceipt.length() == 0) {
						//Local Money Receipt update
						String mid = Integer.toString(receipt.getMid());
						// 0 is the value of voided Status in case of failure in error log table
						Boolean saveFlagMoneyReceipt = MoneyReceiptFetchAndPost(mid,0);
						if(saveFlagMoneyReceipt) {
							SaveStatusOfEachOnSync("Money Receipt", "success", mid);					
						}
						String timestampOfMoneyreceipt = Context.getService(SHRActionAuditInfoService.class)
								.getTimeStampForMoneyReceipt(mid);
						String timestampUpdate = Context.getService(SHRActionAuditInfoService.class)
						.updateAuditMoneyReceipt(timestampOfMoneyreceipt);
					//}
				}catch(Exception e){
					String midEx = Integer.toString(receipt.getMid());
					errorLogInsert("Money Receipt",e.toString(),midEx,0);
				}
			}
	}
	public void sendFailedMoneyReceipt(){
		List<SHRActionErrorLog> failedReceipts = Context.getService(SHRActionErrorLogService.class)
				.get_list_by_Action_type("Money Receipt");
//		errorLogUpdate("Money Receipt List","list size check",Integer.toString(failedReceipts.size()));
//		errorLogUpdate("Loop Starts","Loop Starts",UUID.randomUUID().toString());
		for(SHRActionErrorLog receipt: failedReceipts){
			boolean status = true;
			try{
				String globalServerUrl = centralServer + "openmrs/ws/rest/v1/visittype";
				String get_result = HttpUtil.get(globalServerUrl, "", "admin:test"); 
				
			}catch(Exception e){
				e.printStackTrace();
				status = false;
			}
			if(status) {
			String mid = receipt.getUuid();
			Boolean flag = false;
//			errorLogUpdate("Money Receipt Test","Status:"+receipt.getSent_status(),mid);
				if(receipt.getSent_status() == 0){
					// +1 for status incrementing
					int val = receipt.getVoided()+1;
					Boolean sentFlag = MoneyReceiptFetchAndPost(mid, val > 1 ? 2 : val);
					log.error("sentFlag money receipt" + mid);
					Context.getService(SHRActionErrorLogService.class).
						updateSentStatus(receipt.getEid(), sentFlag == true? 1 : 0);
					
				}
			}
		}
	}
	
	private Boolean MoneyReceiptFetchAndPost(String mid,int voidedStatus){
		log.error("Entering to fetch money receipt" + mid);
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
				if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
					errorLogInsert("Money Receipt","Money Receipt Get:"+e.toString(),mid,voidedStatus == 2 ? 1 : voidedStatus);
				}
				else {
					errorLogInsert("Money Receipt","Money Receipt Get:"+e.toString(),mid,voidedStatus);
				}
				
				return false;
			}
			
			
			//JSON Money Receipt Update to Central Server
			String postMoneyReceipt = "";
			 try{
				 postMoneyReceipt = moneyReceiptConverter(moneyReceipt);
			 }catch(Exception e){
				if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
					errorLogInsert("Money Receipt",e.toString(),mid,voidedStatus == 2 ? 1 : voidedStatus);
				}
				else {
					errorLogInsert("Money Receipt",e.toString(),mid,voidedStatus);
				} 			 
				 return false;
			 }
//			errorLogUpdate("Money Receipt Format Post",postMoneyReceipt,mid);
			String centralPostUrl = centralServer+"openmrs/ws/rest/v1/money-receipt/save-in-global";
			//IF success update timestamp
			String postAction = "";
			try{
				log.error("trying to post money receipt" + mid);
				postAction = HttpUtil.post(centralPostUrl, "", postMoneyReceipt);
				JSONObject returnedResultOfmoneyReceipt = new JSONObject(postAction);
				log.error("returnedResultOfmoneyReceipt" + returnedResultOfmoneyReceipt.toString());
				String sentStatus = returnedResultOfmoneyReceipt.getString("isSuccess");
				if(sentStatus.equalsIgnoreCase("true")) {
					return true;
				}
				else if(sentStatus.equalsIgnoreCase("false")){
					   String message = returnedResultOfmoneyReceipt.getString("message");
					   errorLogInsert("Money Receipt","Money Receipt failed while posting: "+message +"  .with Post Json "+ postAction,mid,voidedStatus);
					   return false;
				}
			}catch(Exception e){
				log.error("failed to post money receipt" + mid);
				if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
					errorLogInsert("Money Receipt","Money Receipt Post:"+e.toString(),mid,voidedStatus == 2 ? 1 : voidedStatus);
				}
				else {
					errorLogInsert("Money Receipt","Money Receipt Post:"+e.toString(),mid,voidedStatus);
				}
				return false;
			}
			
//			if(!"".equalsIgnoreCase(postAction)){
//				if(voidedStatus == 0){
//				String timestamp=Context.getService(SHRActionAuditInfoService.class)
//						.getTimeStampForMoneyReceipt(mid);
////				if(!"".equalsIgnoreCase(timestamp))
//					String timestampUpdate = Context.getService(SHRActionAuditInfoService.class)
//				.updateAuditMoneyReceipt(timestamp);
//				}
//			}
//			else {
//				errorLogInsert("Money Receipt","Money Receipt Post:"+postAction,mid,voidedStatus);
//				return false;
//			}
		}catch(Exception e){
			log.error("in try catch after failed to post money receipt" + mid);
			if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
				errorLogInsert("Money Receipt",e.toString(),mid,voidedStatus == 2 ? 1 : voidedStatus);
			}
			else {
				errorLogInsert("Money Receipt",e.toString(),mid,voidedStatus);
			}
			
			return false;
		}
		log.error("in the verge of returning status " + mid);
		return true;
	}
	//<param>
		//patientUuid - patient's identity key.
		//id - of event records - for updating last index of a table.
		//failedPatient - flag to check which kind of encounter it is.
	//</param>
	private Boolean patientFetchAndPost(String patientUUid,String id,int voidedStatus) throws ParseException, JSONException{
			String clinicCode = Context.getService(SHRActionAuditInfoService.class).getClinicCodeForClinic(patientUUid);
			JSONParser jsonParser = new JSONParser();
		
			// Get Patient Info from Local Server
			String patientUrl = localServer+"openmrs/ws/rest/v1/patient/"+
					patientUUid+"?v=full";

			String patientResponse = "";
			try{
				patientResponse = HttpUtil.get(patientUrl, "", "admin:test");								
			}catch(Exception e){
				if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
					errorLogInsert("Patient","Patient Get Error"+e.toString(),patientUUid,voidedStatus == 2 ? 1 : voidedStatus);
				}
				else {
					errorLogInsert("Patient","Patient Get Error"+e.toString(),patientUUid,voidedStatus);
				}
				
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
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Patient","Server Patient Check: "+ e.toString(),patientUUid,voidedStatus == 2 ? 1 : voidedStatus);
					}
					else {
						errorLogInsert("Patient","Server Patient Check: "+ e.toString(),patientUUid,voidedStatus);
					}
					return false;
				}
				JSONObject patienResponseCheck = new JSONObject(centralServerPatientCheckResponse);
				
				//If Error No String concat as Add Action - Else Update action API
				patientPostUrl += patienResponseCheck.has("error") ? "" : "/"+patientUUid;
				String returnedResult = "";
								
				try{ 
					
					returnedResult = HttpUtil.post(patientPostUrl, "", postData);
					JSONObject returnedResultOfPatient = new JSONObject(returnedResult);
					if(returnedResultOfPatient.has("error")) {
						JSONObject errorMessageObject = (JSONObject) returnedResultOfPatient.get("error");
						String errorMessage = errorMessageObject.getString("message");
						errorLogInsert("Patient","Global Server Patient Save Error:" + errorMessage,patientUUid,voidedStatus);
						return false;
					}
					else {
//					errorLogUpdate("patient post",returnedResult,patientUUid);
					//origin table will be inserted in global server for addition only
						if(patienResponseCheck.has("error")){
							String insertUrl = centralServer+"openmrs/ws/rest/v1/save-Patient/insert/patientOriginDetails";
								insertUrl += "?patient_uuid="+patientUUid+"&patient_origin="+clinicCode+"&syncStatus="+ServerAddress.sendToDhisFromGlobal+"&type=patient_uuid&encounter_uuid=0";
							log.error("Insert url" + insertUrl);
							String get = "";
							try{
								get = HttpUtil.get(insertUrl, "", "admin:test");
							}catch(Exception e){
								if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
									errorLogInsert("Patient","Network is unreachable (connect failed):" + e.toString(),patientUUid,voidedStatus == 2 ? 1 : voidedStatus);
								}
								else {
									errorLogInsert("Patient","Global Server Origin Save Error:" + e.toString(),patientUUid,voidedStatus);
								}
								
								return false;
							}
						}
					}
				
				}catch(Exception e){
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus == 2 ? 1 : voidedStatus);
					}
					else {
						errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus);
					}
					return false;
				}
				// Save last entry in Audit Table
//				if(voidedStatus == 0){
//					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
//						.updateAuditPatient(id);
//				}
				
			}catch(Exception e){
				if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
					errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus == 2 ? 1 : voidedStatus);
				}
				else {
					errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus);
				}
				return false;
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
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Encounter","Encounter get Error:"+response,encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
					}
					else {
						errorLogInsert("Encounter","Encounter get Error:"+response,encounterUuid,voidedStatus);
					}					
					return false;
				}
				
				//Encounter Response Formatting
				//JSONObject encounterResponse = new JSONObject(response);
				org.json.simple.JSONObject enc_response = new org.json.simple.JSONObject();
				try{
				 enc_response = (org.json.simple.JSONObject) jsonParser.
						parse(response);
				}catch(Exception e){
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Encounter",e.toString(),encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
					}
					else {
						errorLogInsert("Encounter",e.toString(),encounterUuid,voidedStatus);
					}
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
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Encounter","Encounter Search Error"+e.toString(),encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
					}
					else {
						errorLogInsert("Encounter","Encounter Search Error"+e.toString(),encounterUuid,voidedStatus);
					}
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
						if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
							errorLogInsert("Encounter","Encounter Visit Response:"+e.toString()
									,encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
						}
						else {
							errorLogInsert("Encounter","Encounter Visit Response:"+e.toString()
									,encounterUuid,voidedStatus);
						}
						return false;
					}
					
					org.json.simple.JSONObject visit_response = new org.json.simple.JSONObject();
					//Visit Response Parsing
					try{
					 visit_response = (org.json.simple.JSONObject) jsonParser.parse(vis_response);
					}catch(Exception e){
						if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
							errorLogInsert("Encounter","Encounter Visit Json Parse Error:"+e.toString(),
									encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
						}
						else {
							errorLogInsert("Encounter","Encounter Visit Json Parse Error:"+e.toString(),
									encounterUuid,voidedStatus);
						}
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
						if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
							errorLogInsert("Encounter","Create Visit Error:"+e.toString(),
									encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
						}
						else {
							errorLogInsert("Encounter","Create Visit Error:"+e.toString(),
									encounterUuid,voidedStatus);
						}
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

				enc_response.put("locationUuid", "8d6c993e-c2cc-11de-8d13-0010c6dffd0f");
				
				if(enc_response.containsKey("providers")) {
					org.json.simple.JSONArray  providerArray = (org.json.simple.JSONArray) enc_response.get("providers");
					for (int i = 0; i < providerArray.size(); i++) {
						org.json.simple.JSONObject providerObject = (org.json.simple.JSONObject) providerArray.get(i);
						String providerUuid = "c1c26908-3f10-11e4-adec-0800271c1b75";
						providerObject.remove("uuid");
						providerObject.put("uuid", providerUuid);
					}
				}
				
				//Encounter Post
				String postUrl = centralServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
				
				//Finding Add or Update Action for Post Url
				
				org.json.simple.JSONArray obs = SharedHealthRecordManageRestController.getObservations((org.json.simple.JSONArray)enc_response.get("observations"));
//				enc_response.remove("observations");
				org.json.simple.JSONObject encounter = (org.json.simple.JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(enc_response.toString(),Encounter.class)));
				
				//for removing duplicity of lab order 
				Set<String> disContinueConceptUuid = new HashSet<String>();
				org.json.simple.JSONArray labOrders = (org.json.simple.JSONArray) encounter.get("orders");
				org.json.simple.JSONArray customLabOrder = new org.json.simple.JSONArray();
				org.json.simple.JSONArray finalFilteredLabOrder = new org.json.simple.JSONArray();
				for (int i = 0; i < labOrders.size(); i++) {
					org.json.simple.JSONObject labOrderObject = (org.json.simple.JSONObject) labOrders.get(i);
					org.json.simple.JSONObject conceptObject = (org.json.simple.JSONObject) labOrderObject.get("concept");
					String uuidConcept = (String) conceptObject.get("uuid");
					String action = (String) labOrderObject.get("action");
					if(action.equalsIgnoreCase("NEW")) {
						customLabOrder.add(labOrders.get(i));
					}
					else if (action.equalsIgnoreCase("DISCONTINUE")) {
						disContinueConceptUuid.add(uuidConcept);
					}
				}
				List<String> list = new ArrayList<String>(disContinueConceptUuid);
				for (int i = 0; i < customLabOrder.size(); i++) {
					for (int j = 0; j < list.size(); j++) {
						org.json.simple.JSONObject labOrderObject = (org.json.simple.JSONObject) customLabOrder.get(i);
						org.json.simple.JSONObject conceptObject = (org.json.simple.JSONObject) labOrderObject.get("concept");
						String uuidConcept = (String) conceptObject.get("uuid");
						String discontinueUuid = list.get(j);
						if(uuidConcept.equalsIgnoreCase(discontinueUuid)) {
							org.json.simple.JSONObject laborderNested = (org.json.simple.JSONObject) customLabOrder.get(i);
							laborderNested.put("voided", true);
						}
					}
				}
				for (int i = 0; i < customLabOrder.size(); i++) {
					org.json.simple.JSONObject labOrderObject = (org.json.simple.JSONObject) customLabOrder.get(i);
					if(!labOrderObject.containsKey("voided")) {
						finalFilteredLabOrder.add(labOrderObject);
					}
				}
				encounter.remove("orders");
				encounter.put("orders", finalFilteredLabOrder);
				//end
				encounter.put("observations", obs);

				//Encounter  Existence Check in Global Server
				String searchEncounterUrl = centralServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"
						+ encounterUuid + "?includeAll=true";
				String globalEncounterResponse = "";
				try{
					globalEncounterResponse = HttpUtil.get(searchEncounterUrl, "", "admin:test");
				}catch(Exception e){
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Encounter","Encounter Global Search error:"+e.toString(),
								encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
					}
					else {
						errorLogInsert("Encounter","Encounter Global Search error:"+e.toString(),
								encounterUuid,voidedStatus);
					}
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
					boolean postStatus = true;
					
					String postResponse = HttpUtil.post(postUrl, "", encounter.toJSONString());
					JSONObject postResponseObject = new JSONObject(postResponse);
					if(postResponseObject.has("error")) {
						JSONObject errorMessageObject = (JSONObject) postResponseObject.get("error");
						String errorMessage = errorMessageObject.getString("message");
						errorLogInsert("Encounter",errorMessage,
								encounterUuid,voidedStatus);
						postStatus = false;
					}
					if(!postStatus) {
						return false;
					}
					else {

						String patientUuid = postResponseObject.getString("patientUuid");
						int statusSync = ServerAddress.sendToDhisFromGlobal;
						List<SHRExternalPatient> patientsToSend = Context.getService(SHRExternalPatientService.class).findByPatientUuid(patientUuid,"patient");
						if(patientsToSend.size() !=0 && ServerAddress.sendToDhisFromGlobal == 1) {
							statusSync = 0;
						}
						String clinicCode = "";
						if(ServerAddress.sendToDhisFromGlobal == 0) {
							clinicCode = "0";
						}
						else {
							clinicCode = Context.getService(SHRActionAuditInfoService.class).getClinicCodeForClinic(patientUuid);
						}
						String insertUrl = centralServer+"openmrs/ws/rest/v1/save-Patient/insert/patientOriginDetails";
							insertUrl += "?patient_origin="+clinicCode+"&syncStatus="+statusSync+"&type=encounter_uuid&encounter_uuid="+encounterUuid+"&patient_uuid=0";
							
						String get = "";
						try{
							get = HttpUtil.get(insertUrl, "", "admin:test");
						}catch(Exception e){
							if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
								errorLogInsert("Encounter","Network is unreachable (connect failed):" + e.toString(),encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
							}
							else {
								errorLogInsert("Encounter","Global Server Origin Save Error:" + e.toString(),encounterUuid,voidedStatus);
							}
							
							return false;
						}
					}
				}catch(Exception e){
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Encounter","Encounter post error:"+e.toString(),
								encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
					}
					else {
						errorLogInsert("Encounter","Encounter post error:"+e.toString(),
								encounterUuid,voidedStatus);
					}
					return false;
				}
				//voided Status Increment
//				if(voidedStatus == 0){					
//					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
//					.updateAuditEncounter(id);					
//				}
			}catch(Exception e){
				if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Encounter","Encounter Error:"+e.toString(),encounterUuid,voidedStatus == 2 ? 1 : voidedStatus);
					}
					else {
						errorLogInsert("Encounter","Encounter Error:"+e.toString(),encounterUuid,voidedStatus);
					}
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
//		if(voided > 0) {
//			Context.getService(SHRActionErrorLogService.class)
//				.delete_by_type_and_uuid(action_type, uuId);
//		}
		//Insert Log
		SHRActionErrorLog log  = Context.getService(SHRActionErrorLogService.class).getErrorByActionTypeAndIdWithSentStatus(action_type, uuId);
		if(log == null) {
			log = new SHRActionErrorLog();
		}
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
	
	
	public void SaveStatusOfEachOnSync(String action_type,String message,String uuId){
		Context.clearSession();
		Context.openSession();
		SHRActionErrorLog log = new SHRActionErrorLog();
		log.setAction_type(action_type);
		log.setError_message(message);
		log.setUuid(uuId);
		log.setVoided(0);
		log.setSent_status(1);
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
		JSONArray jsonNestedPostPayments = new JSONArray();
		JSONArray jsonNestedGetServices = jsonMoneyReceipt.getJSONArray("services");
		JSONArray jsonNestedGetPayments = jsonMoneyReceipt.getJSONArray("payments");
		//Money Receipt Part
		JSONObject jsonNestedGetMoneyReceipt = jsonMoneyReceipt.getJSONObject("moneyReceipt");
		jsonNestedPostMoneyReceipt.put("clinicName", jsonNestedGetMoneyReceipt.
				get("clinicName"));
		jsonNestedPostMoneyReceipt.put("clinicCode",jsonNestedGetMoneyReceipt.get("clinicCode"));
		jsonNestedPostMoneyReceipt.put("orgUnit",jsonNestedGetMoneyReceipt.get("orgUnit"));
		if(jsonNestedGetMoneyReceipt.has("slipNo")) {
			String slipNo = (String) jsonNestedGetMoneyReceipt.get("slipNo");
			if(slipNo != null && !slipNo.isEmpty()) {
				jsonNestedPostMoneyReceipt.put("slipNo", slipNo);
			}
		}
		if(jsonNestedGetMoneyReceipt.has("eslipNo")) {
			String eslipNo = (String) jsonNestedGetMoneyReceipt.get("eslipNo");
			if(eslipNo != null && !eslipNo.isEmpty()) {
				jsonNestedPostMoneyReceipt.put("eslipNo", eslipNo);
			}
		}
		jsonNestedPostMoneyReceipt.put("reference", jsonNestedGetMoneyReceipt.get("reference"));
		if(jsonNestedGetMoneyReceipt.has("referenceId")) {
			String referenceId = (String) jsonNestedGetMoneyReceipt.get("referenceId");
			if(referenceId != null && !referenceId.isEmpty()) {
				jsonNestedPostMoneyReceipt.put("referenceId", referenceId);
			}
		}
		jsonNestedPostMoneyReceipt.put("servicePoint",
				jsonNestedGetMoneyReceipt.get("servicePoint"));
		
		jsonNestedPostMoneyReceipt.put("session",
				jsonNestedGetMoneyReceipt.get("session"));
		if(jsonNestedGetMoneyReceipt.has("other")) {
			jsonNestedPostMoneyReceipt.put("other",
					jsonNestedGetMoneyReceipt.get("other"));
		}
		jsonNestedPostMoneyReceipt.put("sateliteClinicId",
				jsonNestedGetMoneyReceipt.get("sateliteClinicId"));
		
		jsonNestedPostMoneyReceipt.put("teamNo",
				jsonNestedGetMoneyReceipt.get("teamNo"));
		
		if(jsonNestedGetMoneyReceipt.has("cspId")) {
			String cspId = (String) jsonNestedGetMoneyReceipt.get("cspId");
			if(cspId != null && !cspId.isEmpty()) {
				jsonNestedPostMoneyReceipt.put("cspId", cspId);
			}
		}
		
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
		
		jsonNestedPostMoneyReceipt.put("overallDiscount", 
				jsonNestedGetMoneyReceipt.get("overallDiscount").toString());
		jsonNestedPostMoneyReceipt.put("dueAmount", 
				jsonNestedGetMoneyReceipt.get("dueAmount").toString());
		
		jsonPostMoneyReceipt.put("moneyReceipt", jsonNestedPostMoneyReceipt);
		
		//JSON services semi part
		for(int i = 0; i < jsonNestedGetServices.length();i++) {
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
			if(service.has("type")) {
				String type = (String) service.get("type");
				if(type != null && !type.isEmpty()) {
					servicePost.put("type",  type);
				}
			}
			if(service.has("packageUuid")) {
				String packageUuid = (String) service.get("packageUuid");
				if(packageUuid != null && !packageUuid.isEmpty()) {
					servicePost.put("packageUuid",  service.get("packageUuid"));
				}
			};
			if(service.has("financialDiscount")) {
				servicePost.put("financialDiscount", service.get("financialDiscount"));
			}
			if(service.has("sendToDhisFromGlobal")) {
				servicePost.put("sendToDhisFromGlobal", ServerAddress.sendToDhisFromGlobal);
			}
			servicePost.put("category", service.get("category"));
			servicePost.put("totalAmount", service.get("totalAmount").toString());
			servicePost.put("netPayable", service.get("netPayable"));
			servicePost.put("uuid", service.get("uuid"));
			jsonNestedPostServices.put(servicePost);
		}
		jsonPostMoneyReceipt.put("services", jsonNestedPostServices);
		
		for(int i = 0; i < jsonNestedGetPayments.length();i++){
			JSONObject paymentObject = jsonNestedGetPayments.getJSONObject(i);
			JSONObject paymentPost = new JSONObject();
			paymentPost.put("receiveDate",paymentObject.get("receiveDate"));
			paymentPost.put("receiveAmount", paymentObject.get("receiveAmount"));
			paymentPost.put("uuid", paymentObject.get("uuid"));
			jsonNestedPostPayments.put(paymentPost);
		}
		jsonPostMoneyReceipt.put("payments", jsonNestedPostPayments);

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
	
	
	public void sendFollowUpDataToGlobal() {
		try{
			String localGetUrl = localServer+"openmrs/ws/rest/v1/followup/get/followUpList";
			String followUpList = HttpUtil.get(localGetUrl,"","admin:test");
			JSONArray getFollowUpJsonArrayList = new JSONArray(followUpList);
			for(int i = 0; i < getFollowUpJsonArrayList.length();i++){
				JSONObject followUpJsonObject = getFollowUpJsonArrayList.getJSONObject(i);
				String followUpUuid = "";
				if(followUpJsonObject.has("uuid")) {
					followUpUuid = followUpJsonObject.getString("uuid");
				}
				String centralPostUrl = centralServer+"openmrs/ws/rest/v1/followup/save-update-in-global";
				String postAction = HttpUtil.post(centralPostUrl, "", followUpJsonObject.toString());
				JSONObject postFollowUpResult = new JSONObject(postAction);
				if(postFollowUpResult.has("isSuccess")) {
					Boolean isSuccess = postFollowUpResult.getBoolean("isSuccess");
					String message = postFollowUpResult.getString("message");
					if(isSuccess && !message.equalsIgnoreCase("Follow Up Already UpDated")) {
						String uuid = postFollowUpResult.getString("followupUuid");
						SaveStatusOfEachOnSync("Follow-Up","Success", uuid);
					}
					else if(!isSuccess) {
						 errorLogInsert("Follow-Up","Follow Sync Failed From Local To Global:"+ postAction,followUpUuid,2);
					}
				}
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
}
