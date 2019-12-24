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
//	String localServer = "https://192.168.19.147/";
//	String localServer = "http://192.168.33.10/";
	String centralServer="https://192.168.19.147/";
//	String centralServer = "https://192.168.33.10/";
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
			try{
				sendEncounter();
			}catch(Exception e){
				e.printStackTrace();
			}
//			try{
//				sendFailedMoneyReceipt();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
			try{
				sendMoneyReceipt();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		Context.closeSession();
		
	}
	
	public void sendPatient() throws ParseException{
		JSONParser jsonParser = new JSONParser();
		
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
					
					patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
					errorLogUpdate("patient","Patient Update/Add Check",patientUUid);
				} catch (JSONException e) {					
					errorLogUpdate("patient",e.toString(),patientUUid);
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
						errorLogUpdate("patient Update to Central Server",get_result,patientUUid);
						
						patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
						errorLogUpdate("patient","Patient Update/Add Check",patientUUid);
					} catch (JSONException e) {
						errorLogUpdate("patient external Send Error",e.toString(),patientUUid);
					}
				}
				else {
					// do nothing
				}
			}
			
		}
		}catch(Exception e){
			errorLogUpdate("patient",e.toString(),patUuid);
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
				log.setAction_type("Patient");
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
				errorLogUpdate("Encounter",e.toString(),encounter.getUuid());
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
				errorLogUpdate("Money Receipt","Error Check",receipt.getPatient_uuid());
				errorLogUpdate("Money Receipt","Money Receipt Fetch Check",mid);
				MoneyReceiptFetchAndPost(mid,false);
				mid_ = mid.toString();
			}
		}catch(Exception e){
			errorLogUpdate("Money Receipt Error",e.toString(),mid_);
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
		errorLogUpdate("Money Receript Hitting","Method Hits",mid);
		try{
			JSONObject jsonMoneyReceipt = new JSONObject();
			String localGetUrl = localServer+"openmrs/ws/rest/v1/money-receipt"
					+ "/get/"+mid;
			errorLogUpdate("Money Receript Get Url",localGetUrl,mid);
			String moneyReceipt = HttpUtil.get(localGetUrl,"","admin:test");
			
			errorLogUpdate("Money Receipt Get Check",moneyReceipt,mid);
			
			
			//JSON Money Receipt Update to Central Server
			String postMoneyReceipt = "";
			 try{
				 postMoneyReceipt = moneyReceiptConverter(moneyReceipt);
			 }catch(Exception e){
				 errorLogUpdate("Money Receipt",e.toString(),mid);
			 }
			errorLogUpdate("Money Receipt Format Post",postMoneyReceipt,mid);
			String centralPostUrl = centralServer+"openmrs/ws/rest/v1/money-receipt/add-or-update";
			//IF success update timestamp
			String postAction = HttpUtil.post(centralPostUrl, "", postMoneyReceipt);
			errorLogUpdate("Money Receipt Post",postAction,mid);
			
			if(!"".equalsIgnoreCase(postAction)){
				if(failedReceipt == false){
				String timestamp=Context.getService(SHRActionAuditInfoService.class)
						.getTimeStampForMoneyReceipt(mid);
//				if(!"".equalsIgnoreCase(timestamp))
					String timestampUpdate = 	Context.getService(SHRActionAuditInfoService.class)
				.updateAuditMoneyReceipt(timestamp);
				}
			}
			else {
				errorLogUpdate("Money Receipt",postAction,mid);
			}
		}catch(Exception e){
			errorLogUpdate("Money Receipt",e.toString(),mid);
		}
	}
	private void patientFetchAndPost(String patientUUid,String id,Boolean failedPatient) throws ParseException, JSONException{
		
			JSONParser jsonParser = new JSONParser();
		
			// Get Patient Info from Local Server
			String patientUrl = localServer+"openmrs/ws/rest/v1/patient/"+
					patientUUid+"?v=full";
			SHRActionErrorLog log_ = new SHRActionErrorLog();

			String patientResponse = "";
			try{
				patientResponse = HttpUtil.get(patientUrl, "", "admin:test");
				
				errorLogUpdate("patient get",patientResponse,patientUUid);
				
			}catch(Exception e){
				errorLogUpdate("patient get error",e.toString(),patientUUid);
			}

			JSONObject getPatient = new JSONObject(patientResponse);
			try{
				
			
				String personUuid = (String) getPatient.get("uuid");
				
				// Model Conversion for Post into Central Server
				org.json.simple.JSONObject getPatient_ = (org.json.simple.JSONObject)
						jsonParser.parse(getPatient.toString());
				
				String postData = SharedHealthRecordManageRestController.
						getPatientObject(getPatient_, personUuid);
				errorLogUpdate("Patient Post Format Data",postData,patientUUid);
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
				String centralServerPatientCheckResponse = HttpUtil.get(centralServerPatientCheckUrl,
								"", "admin:test");
				JSONObject patienResponseCheck = new JSONObject(centralServerPatientCheckResponse);
				
				//If Error No String concat as Add Action - Else Update action API
				patientPostUrl += patienResponseCheck.has("error") ? "" : "/"+patientUUid;
				String returnedResult = "";
								
				try{ 
					
					returnedResult = HttpUtil.post(patientPostUrl, "", postData);
					errorLogUpdate("patient post",returnedResult,patientUUid);
					String insertUrl = centralServer+"openmrs/ws/rest/v1/save-Patient/insert/patientOriginDetails";
						insertUrl += "?patient_uuid="+patientUUid+"&patient_origin="+localServer;
					String get = "";
					try{
						get = HttpUtil.get(insertUrl, "", "admin:test");
					}catch(Exception e){
						errorLogUpdate("Patient","Local Server Save Info Error",patientUUid);
					}
				
				}catch(Exception e){
					errorLogUpdate("patient post error",e.toString(),patientUUid);
					return;
				}
				// Save last entry in Audit Table
				if(failedPatient == false){
					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
						.updateAuditPatient(id);
				}
				
			}catch(Exception e){
				// Error Log Generation on Exception
				errorLogUpdate("patient",e.toString(),patientUUid);
			}
	
	}
	
	private void encounterFetchAndPost(String encounterUuid, String id,Boolean failedEncounter) throws ParseException{
		JSONParser jsonParser = new JSONParser();
		Boolean visitFlagError = false;
			
			Boolean status = false;
			try{
				String getUrl = localServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"
						+ encounterUuid + "?includeAll=true";
				String response = "";
				try{
					response = HttpUtil.get(getUrl, "", "admin:test");
				}catch(Exception e){
					SHRActionErrorLog logN = new SHRActionErrorLog();
					errorLogUpdate("Encounter Local Get Error",response,encounterUuid);
				}
				
				JSONObject encounterResponse = new JSONObject(response);
				org.json.simple.JSONObject enc_response = new org.json.simple.JSONObject();
				try{
				 enc_response = (org.json.simple.JSONObject) jsonParser.
						parse(encounterResponse.toString());
				}catch(Exception e){
					errorLogUpdate("Encounter Response Error",e.toString(),encounterUuid);
				}
				String visitUuid = enc_response.get("visitUuid").toString();
				String visitFetchUrl = "";
				String vis_global_response = "";
				
				//Central Server Visit Existence Check
				try{
				
					visitFetchUrl = centralServer+
							"openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid="+visitUuid;
					vis_global_response = HttpUtil.get(visitFetchUrl, "","admin:test");
					errorLogUpdate("Encounter visit global fetch",vis_global_response,encounterUuid);
				}catch(Exception e){
					errorLogUpdate("Encounter Visit Global Parse Error",e.toString(),encounterUuid);
				}
				//Central Server Visit Existence Check
		
				JSONParser jsonParser1 = new JSONParser();
				org.json.simple.JSONObject visitFetchJsonObj = (org.json.simple.JSONObject) 
						jsonParser1.parse(vis_global_response);
				
				String vis_response = "";
				//IF Not exist proceed to create Visit on Central Server	
				if(visitFetchJsonObj.get("isFound").toString().contains("false")){
					String vis_url =  localServer+
							"openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid="+visitUuid;
					 vis_response = HttpUtil.get(vis_url, "", "admin:test");
					 errorLogUpdate("Encounter visit Local Fetch",vis_response,encounterUuid);
					org.json.simple.JSONObject visit_response = new org.json.simple.JSONObject();
					try{
					 visit_response = (org.json.simple.JSONObject) jsonParser.parse(vis_response);
					 errorLogUpdate("Visit Json Parse",visit_response.toString(),encounterUuid);
					}catch(Exception e){
						errorLogUpdate("Encounter Visit Json Local parse Error",e.toString(),
								encounterUuid);
					}
					String createVisit_ = "";
					
					try{ 
						createVisit_ = createVisit(visit_response,enc_response.get("patientUuid").toString());
						JSONObject createVisitResponse = new JSONObject(createVisit_);
						errorLogUpdate("Encounter Visit Create Post Response",createVisitResponse.toString(),encounterUuid);
						visitFlagError = createVisitResponse.get("isSuccessfull").toString().contains("true")
											? false: true;
						
						
					}catch(Exception e){
						errorLogUpdate("Encounter Visit Create Error",e.toString(),encounterUuid);	
					}
					
				}
				// If exist on central Server do nothing
				else if(visitFetchJsonObj.get("isFound").toString().contains("true")){
					// do nothing		
				}
				// Create Visit Exception will stop the proceeding process
				if(visitFlagError == true)
					return;
				

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
				
				//Observation Add
				///Observations Needs to be checked
				org.json.simple.JSONArray obs = SharedHealthRecordManageRestController.getObservations((org.json.simple.JSONArray)enc_response.get("observations"));
//				enc_response.remove("observations");
				org.json.simple.JSONObject encounter = (org.json.simple.JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(enc_response.toString(),Encounter.class)));
				encounter.put("observations", obs);
				errorLogUpdate("Encounter Post Json Format",encounter.toString(),encounterUuid);
				try{
				String postResponse = HttpUtil.post(postUrl, "", encounter.toJSONString());
				errorLogUpdate("Encounter Post Response",postResponse,encounterUuid);
				}catch(Exception e){
					errorLogUpdate("Encounter Post Error",e.toString(),encounterUuid);
				}
				
				if(failedEncounter == false){					
					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
					.updateAuditEncounter(id);					
				}
			}catch(Exception e){
				errorLogUpdate("Encouner Audit row update error",e.toString(),encounterUuid);
			}		
		}
	
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

	public static void errorLogUpdate(String type,String message, String uuId){
		Context.clearSession();
		Context.openSession();
		SHRActionErrorLog log = new SHRActionErrorLog();
		log.setAction_type(type);
		log.setError_message(message);
		log.setUuid(uuId);
		Context.getService(SHRActionErrorLogService.class)
			.insertErrorLog(log);
		Context.clearSession();
		Context.openSession();
	}
	
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
	
	
	
}
