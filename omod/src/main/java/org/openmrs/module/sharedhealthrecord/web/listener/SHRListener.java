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
	
	
	String localServer = "https://192.168.19.145/";
	
//	String localServer = "http://192.168.33.10/";
	String centralServer="https://192.168.19.147/";
	public static DateFormat dateFormatTwentyFourHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@SuppressWarnings("rawtypes")
//	@Scheduled(fixedRate=10000)
//	private static final Logger log = LoggerFactory.getLogger(SHRListener.class);
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
//			try{
//				sendFailedPatient();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
			try{
				sendPatient();

			}catch(Exception e){
				e.printStackTrace();
			}
//			try{
//				sendFailedEncounter();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			try{
				sendEncounter();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			try{
//				sendFailedMoneyReceipt();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			try{
//				sendMoneyReceipt();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
		}
		
		Context.closeSession();
		
	}
	
	public void sendPatient() throws ParseException{
		JSONParser jsonParser = new JSONParser();
		
		String last_entry = Context.getService(SHRActionAuditInfoService.class)
				.getLastEntryForPatient();
		
		
		
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
				.getEventRecords("Patient",last_entry);
		///openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full
		
		
		
		try{
			for(EventRecordsDTO rec: records){
		
			
			String patientUUid = rec.getObject().split("/|\\?")[6];
			
//			SHRActionErrorLog logE = new SHRActionErrorLog();
//			logE.setAction_type("loop_Check");
////			logE.setId(Integer.parseInt(id));
//			logE.setError_message(patientUUid);
//			logE.setUuid(UUID.randomUUID().toString());
//			Context.getService(SHRActionErrorLogService.class)
//				.insertErrorLog(logE);
			List<SHRExternalPatient> patientsToSend = Context.
					getService(SHRExternalPatientService.class).
						findByPatientUuid(patientUUid,"patient");
			// If patient is not found in table it must be sent
			if(patientsToSend.size() == 0){				
				try {
					patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					SHRActionErrorLog log_ = new SHRActionErrorLog();
					log_.setAction_type("Patient");
					log_.setId(rec.getId());
					log_.setError_message(e.toString());
					log_.setUuid(patientUUid);
					Context.getService(SHRActionErrorLogService.class)
						.insertErrorLog(log_);
				}
				
			}
			else {
				//If patient is found in table with Is_Send_to_Central = 1, it must be sent
				if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
					try {
						patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						SHRActionErrorLog _log = new SHRActionErrorLog();
						_log.setAction_type("Patient");
//						_log.setId(rec.getId());
						_log.setError_message(e.toString());
						_log.setUuid(patientUUid);
						Context.getService(SHRActionErrorLogService.class)
							.insertErrorLog(_log);
					}
				}
				else {
					// do nothing
				}
			}
			
		}
		}catch(Exception e){
			SHRActionErrorLog log_F = new SHRActionErrorLog();
			log_F.setAction_type("Loop_Catch");
//			log_F.setId(rec.getId());
			log_F.setError_message(e.toString());
			log_F.setUuid(UUID.randomUUID().toString());
			Context.getService(SHRActionErrorLogService.class)
				.insertErrorLog(log_F);
		}
	}
	public void sendFailedPatient() throws ParseException{
		List<SHRActionErrorLog> failedPatients = new ArrayList<SHRActionErrorLog>();
		
		for(SHRActionErrorLog failPat : failedPatients){
			Context.getService(SHRActionErrorLogService.class).
			delete_by_type_and_id("Patient", Integer.toString(failPat.getId()));
			try {
				patientFetchAndPost(failPat.getUuid(),Integer.toString(failPat.getId()),true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				SHRActionErrorLog log = new SHRActionErrorLog();
				log.setAction_type("Patient ");
//				log.setId(failPat.getId());
				log.setError_message(e.toString());
				log.setUuid(failPat.getUuid());
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(log);
			}						
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
//				log.setId(Integer.parseInt(id));
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
			jsonMoneyReceipt = new JSONObject(moneyReceipt);
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
				
				jsonNestedPostServices.put(servicePost);
			}
			jsonPostMoneyReceipt.put("services", jsonNestedPostServices);
			//JSON Money Receipt Update to Central Server
			String centralPostUrl = centralServer+"openmrs/ws/rest/v1/money-receipt/add-or-update";
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
//				log.setId(Integer.parseInt(mid));
				log.setError_message(postAction);
				log.setUuid(UUID.randomUUID().toString());
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(log);
			}
		}catch(Exception e){
			SHRActionErrorLog log = new SHRActionErrorLog();
			log.setAction_type("Money Receipt");
//			log.setId(Integer.parseInt(mid));
			log.setError_message(e.toString());
			log.setUuid(UUID.randomUUID().toString());
			Context.getService(SHRActionErrorLogService.class)
				.insertErrorLog(log);
		}
	}
	private void patientFetchAndPost(String patientUUid,String id,Boolean failedPatient) throws ParseException, JSONException{
		
			JSONParser jsonParser = new JSONParser();
		
			// Get Patient Info from Local Server
			String patientUrl = localServer+"openmrs/ws/rest/v1/patient/"+
					patientUUid+"?v=full";
			SHRActionErrorLog log_ = new SHRActionErrorLog();
//			log_.setAction_type("Patient Get Before");
////			log.setId();
//			log_.setError_message(patientUrl);
//			log_.setUuid(UUID.randomUUID().toString());
//			Context.getService(SHRActionErrorLogService.class)
//				.insertErrorLog(log_);
			String patientResponse = "";
			try{
				patientResponse = HttpUtil.get(patientUrl, "", "admin:test");
				
//				SHRActionErrorLog logJ = new SHRActionErrorLog();
//				logJ.setAction_type("Patient Get Response");
////				log.setId();
//				logJ.setError_message("Ok");
//				logJ.setUuid(UUID.randomUUID().toString());
//				Context.getService(SHRActionErrorLogService.class)
//					.insertErrorLog(logJ);
			}catch(Exception e){
				SHRActionErrorLog logN = new SHRActionErrorLog();
				logN.setAction_type("Patient");
//				log.setId();
				logN.setError_message(e.toString());
				logN.setUuid(UUID.randomUUID().toString());
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(logN);
				return;
			}
//			SHRActionErrorLog log = new SHRActionErrorLog();
//			log.setAction_type("Patient Get");
////			log.setId();
//			log.setError_message("OK");
//			log.setUuid(UUID.randomUUID().toString());
//			Context.getService(SHRActionErrorLogService.class)
//				.insertErrorLog(log);
			JSONObject getPatient = new JSONObject(patientResponse);
			try{
				
			
				String personUuid = (String) getPatient.get("uuid");
				
				// Model Conversion for Post into Central Server
				org.json.simple.JSONObject getPatient_ = (org.json.simple.JSONObject)
						jsonParser.parse(getPatient.toString());
				
				String postData = SharedHealthRecordManageRestController.
						getPatientObject(getPatient_, personUuid);
				
				//Post to Central Ser
//				ver
				String patientPostUrl = centralServer+
						"openmrs/ws/rest/v1/bahmnicore/patientprofile";
				String returnedResult = "";
				
				try{ 
				returnedResult = HttpUtil.post(patientPostUrl, "", postData);
//				SHRActionErrorLog _log = new SHRActionErrorLog();
//				_log.setAction_type("Patient post");
////				_log.setId();
//				_log.setError_message("OK Posted");
//				_log.setUuid(UUID.randomUUID().toString());
//				Context.getService(SHRActionErrorLogService.class)
//					.insertErrorLog(_log);
				}catch(Exception e){
					SHRActionErrorLog _log = new SHRActionErrorLog();
					_log.setAction_type("Patient");
//					_log.setId();
					_log.setError_message(" Not OK Posted");
					_log.setUuid(UUID.randomUUID().toString());
					Context.getService(SHRActionErrorLogService.class)
						.insertErrorLog(_log);
					return;
				}
				// Save last entry in Audit Table
				if(failedPatient == false){
					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
						.updateAuditPatient(id);
					SHRActionErrorLog log1 = new SHRActionErrorLog();
					log1.setAction_type("Audit Info Save Check");
//					log1.setId(Integer.parseInt(id));
					log1.setError_message(audit_info_save);
					log1.setUuid(patientUUid);
					Context.getService(SHRActionErrorLogService.class)
						.insertErrorLog(log1);
				}
				
			}catch(Exception e){
				// Error Log Generation on Exception
				SHRActionErrorLog log1 = new SHRActionErrorLog();
				log1.setAction_type("Patient");
//				log1.setId(Integer.parseInt(id));
				log1.setError_message(e.toString());
				log1.setUuid(patientUUid);
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(log1);
			}
	
	}
	
	private void encounterFetchAndPost(String encounterUuid, String id,Boolean failedEncounter) throws ParseException{
		JSONParser jsonParser = new JSONParser();
			
			
			Boolean status = false;
			try{
				String getUrl = localServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"
						+ encounterUuid + "?includeAll=true";
				String response = "";
				try{
					response = HttpUtil.get(getUrl, "", "admin:test");
				}catch(Exception e){
					SHRActionErrorLog logN = new SHRActionErrorLog();
					logN.setAction_type("Encounter Get Error");
//					log.setId();
					logN.setError_message(e.toString());
					logN.setUuid(UUID.randomUUID().toString());
					Context.getService(SHRActionErrorLogService.class)
						.insertErrorLog(logN);
				}
				
				JSONObject encounterResponse = new JSONObject(response);
				org.json.simple.JSONObject enc_response = new org.json.simple.JSONObject();
				try{
				 enc_response = (org.json.simple.JSONObject) jsonParser.
						parse(encounterResponse.toString());
				}catch(Exception e){
					SHRActionErrorLog logN = new SHRActionErrorLog();
					logN.setAction_type("Encounter JSon Parse Error");
//					log.setId();
					logN.setError_message(e.toString());
					logN.setUuid(UUID.randomUUID().toString());
					Context.getService(SHRActionErrorLogService.class)
						.insertErrorLog(logN);
				}
//				String visitUuid = SharedHealthRecordManageRestController.createVisit(enc_response,"c5854fd7-3f12-11e4-adec-0800271c1b75");
				String visitUuid = enc_response.get("visitUuid").toString();
				String visitFetchUrl = "";
				String vis_global_response = "";
				SHRActionErrorLog logN_ = new SHRActionErrorLog();
				logN_.setAction_type("Encounter Visit Uuid");
//				log.setId();
				logN_.setError_message(visitUuid);
				logN_.setUuid(UUID.randomUUID().toString());
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(logN_);
				try{
					visitFetchUrl = centralServer+
							"openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid="+visitUuid;
					vis_global_response = HttpUtil.get(visitFetchUrl, "","admin:test");
				}catch(Exception e){
					SHRActionErrorLog logN = new SHRActionErrorLog();
					logN.setAction_type("Encounter Search Global Error");
//					log.setId();
					logN.setError_message(e.toString());
					logN.setUuid(UUID.randomUUID().toString());
					Context.getService(SHRActionErrorLogService.class)
						.insertErrorLog(logN);
				}
				
				SHRActionErrorLog logN = new SHRActionErrorLog();
				logN.setAction_type("Encounter JSon Search Done");
//				log.setId();
				logN.setError_message("JSON Search Done");
				logN.setUuid(UUID.randomUUID().toString());
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(logN);
				JSONParser jsonParser1 = new JSONParser();
				org.json.simple.JSONObject visitFetchJsonObj = (org.json.simple.JSONObject) 
						jsonParser1.parse(vis_global_response);
				
				String vis_response = "";
					
				if(visitFetchJsonObj.get("isFound").toString().contains("false")){
					String vis_url =  localServer+
							"openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid="+visitUuid;
					 vis_response = HttpUtil.get(vis_url, "", "admin:test");

					org.json.simple.JSONObject visit_response = new org.json.simple.JSONObject();
					try{
					 visit_response = (org.json.simple.JSONObject) jsonParser.parse(vis_response);
					}catch(Exception e){
						SHRActionErrorLog _logN = new SHRActionErrorLog();
						_logN.setAction_type("Encounter Visit Parse Error");
//						log.setId();
						_logN.setError_message(e.toString());
						_logN.setUuid(UUID.randomUUID().toString());
						Context.getService(SHRActionErrorLogService.class)
							.insertErrorLog(_logN);
					}
					String createVisit_ = "";
					try{ 
						createVisit_ = createVisit(visit_response);
					}catch(Exception e){
						SHRActionErrorLog log_N = new SHRActionErrorLog();
						log_N.setAction_type("Encounter Visit Create Error");
//						log.setId();
						log_N.setError_message(e.toString());
						log_N.setUuid(UUID.randomUUID().toString());
						Context.getService(SHRActionErrorLogService.class)
							.insertErrorLog(log_N);	
					}
					
					SHRActionErrorLog _logN_ = new SHRActionErrorLog();
					_logN_.setAction_type("Encounter Create Visit Complete");
//					log.setId();
					_logN_.setError_message("Error Create Visit Complete");
					_logN_.setUuid(UUID.randomUUID().toString());
					Context.getService(SHRActionErrorLogService.class)
						.insertErrorLog(_logN_);
				}
				else if(visitFetchJsonObj.get("isFound").toString().contains("true")){
					// do nothing
					SHRActionErrorLog _logN_ = new SHRActionErrorLog();
					_logN_.setAction_type("Found");
//					log.setId();
					_logN_.setError_message("Else section");
					_logN_.setUuid(UUID.randomUUID().toString());
					Context.getService(SHRActionErrorLogService.class)
						.insertErrorLog(_logN_);
				}

				SHRActionErrorLog _logN_ = new SHRActionErrorLog();
				_logN_.setAction_type("parse");
//				log.setId();
				_logN_.setError_message("visit parse");
				_logN_.setUuid(UUID.randomUUID().toString());
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(_logN_);
				String visitTypeValue =SharedHealthRecordManageRestController.visitTypeMapping.get(enc_response.get("visitTypeUuid").toString());
				enc_response.remove("visitTypeUuid");
				enc_response.put("visitType", visitTypeValue);
				
				org.json.simple.JSONArray obs = SharedHealthRecordManageRestController.getObservations((org.json.simple.JSONArray)enc_response.get("observations"));
				enc_response.remove("observations");
				enc_response.put("observations", obs);
				
				if(enc_response.containsKey("locationUuid"))
				{
					enc_response.remove("locationUuid");
				}
				if(enc_response.containsKey("location"))
					enc_response.remove("location");

				enc_response.put("location", "8d6c993e-c2cc-11de-8d13-0010c6dffd0f");
				
				String postUrl = centralServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
				
				org.json.simple.JSONObject encounter = (org.json.simple.JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(enc_response.toString(),Encounter.class)));
				try{
				String postResponse = HttpUtil.post(postUrl, "", encounter.toJSONString());
				}catch(Exception e){
					SHRActionErrorLog logN2 = new SHRActionErrorLog();
					logN2.setAction_type("Encounter Post Encounter Error");
//					logN.setId();
					logN2.setError_message(e.toString());
					logN2.setUuid(UUID.randomUUID().toString());
					Context.getService(SHRActionErrorLogService.class)
						.insertErrorLog(logN2);
				}
				
				if(failedEncounter == false){
					
						String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
						.updateAuditEncounter(id);
						SHRActionErrorLog log1 = new SHRActionErrorLog();
						log1.setAction_type("Audit Info Save Check Encounter");
//						log1.setId(Integer.parseInt(id));
						log1.setError_message(audit_info_save);
						log1.setUuid(UUID.randomUUID().toString());
						Context.getService(SHRActionErrorLogService.class)
							.insertErrorLog(log1);
					
				}
			}catch(Exception e){
				SHRActionErrorLog log = new SHRActionErrorLog();
				log.setAction_type("Encounter Error");
//				log.setId(Integer.parseInt(id));
				log.setError_message(e.toString());
				log.setUuid(encounterUuid);
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(log);
			}		
		}
	
	private String createVisit(org.json.simple.JSONObject obj){
		String visitSavingResponse = "";
		obj.remove("isFound");
		
		try {
			
			String visitSavingUrl = centralServer + "openmrs/ws/rest/v1/save-Patient/insert/patientVisitDetails";

			
			visitSavingResponse = HttpUtil.post(visitSavingUrl, "", obj.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return visitSavingResponse;
	}

//	@Override
//	public void execute() {
//		// TODO Auto-generated method stub
//		try{
//			SHRActionErrorLog log = new SHRActionErrorLog();
//			log.setAction_type("error");
//			log.setError_message("I am hitting");
//			Context.getService(SHRActionErrorLogService.class)
//			.insertErrorLog(log);
//			sendAllData();
//			super.startExecuting();
//		}catch(Exception e){
//			e.printStackTrace();
//		}finally{
//			this.stopExecuting();
//		}
//	}
	
	
	
	
	
	
}
