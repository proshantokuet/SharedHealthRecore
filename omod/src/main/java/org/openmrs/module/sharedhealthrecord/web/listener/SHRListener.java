package org.openmrs.module.sharedhealthrecord.web.listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.module.sharedhealthrecord.utils.ServerAddress;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@EnableScheduling
@Configuration
@EnableAsync
@Controller
public class SHRListener{
	
	String localServer = ServerAddress.localServer();
	String centralServer = ServerAddress.centralServer();
	String isDeployInGlobal = ServerAddress.isDeployInGlobal;
	public static DateFormat dateFormatTwentyFourHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final ReentrantLock lock = new ReentrantLock();

	@SuppressWarnings("rawtypes")
//	@Scheduled(fixedRate=10000)
	private static final Logger log = LoggerFactory.getLogger(SHRListener.class);
	public void sendAllData() throws Exception {
		if (!lock.tryLock()) {
			log.error("It is already in progress.");
	        return;
		}
		log.error("isDeployInGlobal " + isDeployInGlobal);
		if(isDeployInGlobal.equalsIgnoreCase("0")) {
			Context.openSession();
			
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
					sendPatient();
	
				}catch(Exception e){
					e.printStackTrace();
				}
				finally {
					lock.unlock();
					log.error("complete listener patient at:" +new Date());
				}

			}
			
			Context.closeSession();
		}
	}
	
	public synchronized void sendPatient() throws ParseException{
		
		String last_entry = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForPatient();
		
		String patUuid = "";
		
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
				.getEventRecords("Patient",last_entry);
						
		try{
			for(EventRecordsDTO rec: records){
					
			String patientUUid = rec.getObject().split("/|\\?")[6];
			patUuid = patientUUid;
			//Init voidedStatus will be 0 in this case
			Boolean saveFlagPatient = patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),0);
			if(saveFlagPatient) {
				SaveStatusOfEachOnSync("Patient", "success", patientUUid);						
			}
			Context.getService(SHRActionAuditInfoService.class)
					.updateAuditPatient(Integer.toString(rec.getId()));
			}
		} catch(Exception e){
			errorLogInsert("Patient",e.toString(),patUuid,0,"");
		}
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
				if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
					errorLogInsert("Patient","Patient Get Error"+e.toString(),patientUUid,voidedStatus == 2 ? 1 : voidedStatus,"");
				}
				else {
					errorLogInsert("Patient","Patient Get Error"+e.toString(),patientUUid,voidedStatus,"");
				}
				
				return false;
			}

			JSONObject getPatient = new JSONObject(patientResponse);
			try{
				
			
				String personUuid = (String) getPatient.get("uuid");
				
				// Model Conversion for Post into Central Server
				org.json.simple.JSONObject getPatient_ = (org.json.simple.JSONObject)
						jsonParser.parse(getPatient.toString());
				
				String postData = JSONDataCoverter.
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
						errorLogInsert("Patient","Server Patient Check: "+ e.toString(),patientUUid,voidedStatus == 2 ? 1 : voidedStatus,"");
					}
					else {
						errorLogInsert("Patient","Server Patient Check: "+ e.toString(),patientUUid,voidedStatus,"");
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
						errorLogInsert("Patient","Global Server Patient Save Error:" + errorMessage,patientUUid,voidedStatus,postData);
						return false;
					}
				
				}catch(Exception e){
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus == 2 ? 1 : voidedStatus,postData);
					}
					else {
						errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus,postData);
					}
					return false;
				}

				
			}catch(Exception e){
				if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
					errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus == 2 ? 1 : voidedStatus,"");
				}
				else {
					errorLogInsert("Patient",e.toString(),patientUUid,voidedStatus,"");
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
	
	//For creating visit in encounter sending

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
	
	public void errorLogInsert(String action_type,String message,String uuId,Integer voided,String postJson){
		Context.clearSession();
		Context.openSession();
		SHRActionErrorLog log  = Context.getService(SHRActionErrorLogService.class).getErrorByActionTypeAndIdWithSentStatus(action_type, uuId);
		if(log == null) {
			log = new SHRActionErrorLog();
		}
		log.setAction_type(action_type);
		log.setError_message(message);
		log.setUuid(uuId);
		log.setVoided(voided);
		log.setPostJson(postJson);
		//Insert will be called on exception 
		//So 0 - will be inserted automatically
		log.setSent_status(0);
		Context.getService(SHRActionErrorLogService.class).insertErrorLog(log);
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

}
