package org.openmrs.module.sharedhealthrecord.web.listener;

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
import org.openmrs.module.sharedhealthrecord.SHRExternalEncounter;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.domain.Encounter;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.module.sharedhealthrecord.utils.ServerAddress;
import org.openmrs.module.sharedhealthrecord.web.controller.rest.SharedHealthRecordManageRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Service
@EnableScheduling
@Configuration
@EnableAsync
@Controller
public class SHRPatientFetchListener {
	String localServer = ServerAddress.localServer();
	String centralServer = ServerAddress.centralServer();
	String isDeployInGlobal = ServerAddress.isDeployInGlobal;
	private static final Logger log = LoggerFactory.getLogger(SHRPatientFetchListener.class);
	public void fetchAndUpdatePatient(){
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
			if(status) {
				try{
					patientFetchAndUpdateExecute();
				}catch(Exception e){
					errorLogUpdate("Patient Fetch Problem",e.toString(),UUID.randomUUID().toString());
				}
				try{
					encounterFetchAndUpdateExecute();
				}catch(Exception e){
					errorLogUpdate("Encounter Fetch Problem",e.toString(),UUID.randomUUID().toString());
				}
				try{
					deleteLocalMoneyReceipt();
				}catch(Exception e){
					errorLogUpdate("Voided Money Receipt Fetch Problem",e.toString(),UUID.randomUUID().toString());
				}
			}
	
			Context.closeSession();
		}
	}
	
	public void patientFetchAndUpdateExecute(){
		//fetch patient 
		List<String> patientUuidList = new ArrayList<String>();
		String postPatientResponse = "";
		//Getting the changed local patient which is in the global server
		try{
			patientUuidList = getPatientUuidList();
		}catch(Exception e){
//			errorLogUpdate("Patient Uuid Fetch", e.toString(),UUID.randomUUID().toString());
			return;
		}
		//Iterating patient list
		for(String patientUuid: patientUuidList){
			String patient = "";
			try {
				 // Fetching patient info
				 patient = getPatientInfo(patientUuid);		 
			} catch (JSONException | ParseException e) {
				errorLogUpdate("Patient Uuid Fetch",e.toString(),patientUuid);
				e.printStackTrace();
				break;
			}
			//If Json error loop will be incremented
			if(patient.contains("error")) continue;
			try{
				postPatientResponse = postPatientToLocalServer(patient,patientUuid);
				JSONObject response = new JSONObject(postPatientResponse);
//				if(response.has("object")){
					//Update Shr_external_patient to global as 0
					updateExternalPatient(patientUuid);
//				}
			}catch(Exception e){
					errorLogUpdate("Patient Uuid Fetch", e.toString(), patientUuid);
			}
			
		}
	}
	
	private List<String> getPatientUuidList() throws JSONException{
		String clinicCode = Context.getService(SHRActionAuditInfoService.class).getClinicCodeForClinic();
		List<String> patientUuidList = new ArrayList<String>();
		String url = centralServer + 
				"openmrs/ws/rest/v1/save-Patient/search/patientOriginByOriginName?"
				+ "originName="+clinicCode+"&actionType=patient";
		log.error("patient external Check"+url);
		String patientList = HttpUtil.get(url, "", "admin:test");
		JSONArray getPatientList = new JSONArray(patientList);
		
		for(int i = 0; i < getPatientList.length();i++){
			JSONObject patientObject = getPatientList.getJSONObject(i);
			patientUuidList.add(patientObject.get("patient_uuid").toString());
		}
		return patientUuidList;
	}
	
	private String getPatientInfo(String patientUuid) throws JSONException, ParseException{
		String patient = "";
		JSONParser jsonParser = new JSONParser();
		String url = centralServer+"openmrs/ws/rest/v1/patient/"+
				patientUuid+"?v=full";
		patient = HttpUtil.get(url, "", "admin:test");
		
		
		JSONObject patientJSON = new JSONObject(patient);
		String personUuid = (String) patientJSON.get("uuid");
		
		org.json.simple.JSONObject patientJSONPost = (org.json.simple.JSONObject)
				jsonParser.parse(patientJSON.toString());
		
		
		String postData = SharedHealthRecordManageRestController.
				getPatientObject(patientJSONPost, personUuid);
//		errorLogUpdate("Patient Post Format Data",postData,patientUuid);
		
		return postData;
	}
	
	private String postPatientToLocalServer(String postPatient,String patientUuid){
		String url = localServer+
				"openmrs/ws/rest/v1/bahmnicore/patientprofile/"+patientUuid;
		String post = "";
		try{
		 post = HttpUtil.post(url, "",postPatient);
		}catch(Exception e){
			errorLogUpdate("Patient Fetch Uuid",e.toString(),patientUuid);
			
		}
		return post;
	}
	
	private void updateExternalPatient(String patientUuid){
		String externalPatientUpdateUrl = centralServer + 
				"openmrs/ws/rest/v1/save-Patient/insert/"
				+ "externalPatient?patient_uuid="
					+patientUuid+"&action_status=0";
		String get_result = HttpUtil.get(externalPatientUpdateUrl, "", "admin:test");
//		errorLogUpdate("patient Update to Central Server",get_result,patientUuid);
	}
	
	public void encounterFetchAndUpdateExecute() throws JSONException, JsonSyntaxException, ParseException{
		List<SHRExternalEncounter> encounterUuidList = new ArrayList<SHRExternalEncounter>();
		String postEncounterResponse = "";
		try{
			encounterUuidList = getEncounterUuidList();
		}catch(Exception e){
//			errorLogUpdate("Encounter Uuid Fetch", e.toString(),UUID.randomUUID().toString());
			return;
		}
		
		for(SHRExternalEncounter _encounter: encounterUuidList){
			String encounter = "";
			String patientUuid = _encounter.getPatientUuid();
			String encounterUuid = _encounter.getEncounterUuid();
			try{
				encounter = getEncounterInfo(encounterUuid);
				
			}catch(Exception e){
				errorLogUpdate("Encounter Fetch Uuid",e.toString(),encounterUuid);
				return;
			}
			//If encounter has error loop will be incremented
			if(encounter.contains("error")) continue;
			JSONParser jsonParser = new JSONParser();
			//Encounter Response Parsing
			org.json.simple.JSONObject enc_response = new org.json.simple.JSONObject();
			try {
				 enc_response = (org.json.simple.JSONObject) jsonParser.
							parse(encounter);
			} catch ( ParseException e1) {
				errorLogUpdate("Encounter Fetch Uuid",e1.toString(),encounterUuid);
				return;
			}
			
			Boolean visitCreate = createVisit(enc_response.get("visitUuid").toString(),encounterUuid,enc_response.get("patientUuid").toString());
			log.error("Visit Create Status: "+visitCreate.toString());
			if(visitCreate == false) return;
			
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
			//Encounter  Existence Check in Global Server
			String searchEncounterUrl = localServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"
					+ encounterUuid + "?includeAll=true";
			String globalEncounterResponse = "";
			try{
				globalEncounterResponse = HttpUtil.get(searchEncounterUrl, "", "admin:test");
				log.error("Global Search Encounter: "+globalEncounterResponse);
			}catch(Exception e){
				errorLogUpdate("Encounter Fetch Uuid","Encounter Global Search error:"+e.toString(),
						encounterUuid);
				return;
			}
			
			//Json Parsing
			JSONObject globalSearchEncounter = new JSONObject(globalEncounterResponse);
			
			
			//If found on Global Server then delete Encounter
			if(globalSearchEncounter.has("encounterUuid")){
				deleteEncounter(encounterUuid);
			}
			//Encounter Post
			String postUrl = localServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
			
			org.json.simple.JSONArray obs = SharedHealthRecordManageRestController.getObservations((org.json.simple.JSONArray)enc_response.get("observations"));
//			enc_response.remove("observations");
			org.json.simple.JSONObject encounter_ = (org.json.simple.JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(enc_response.toString(),Encounter.class)));
			encounter_.put("observations", obs);
			
			try{
				String postResponse = HttpUtil.post(postUrl, "", encounter_.toJSONString());
				JSONObject postResponseObject = new JSONObject(postResponse);
				if(postResponseObject.has("error")) {
					JSONObject errorMessageObject = (JSONObject) postResponseObject.get("error");
					String errorMessage = errorMessageObject.getString("message");
					errorLogUpdate("Encounter Fetch Uuid","Encounter post error:"+ errorMessage,
							encounterUuid);
				}
				else {
					updateExternalEncounter(patientUuid,encounterUuid);
				}
			}catch(Exception e){
				errorLogUpdate("Encounter Fetch Uuid","Encounter post error:"+e.toString(),
						encounterUuid);
				return;
			}
			
		}
	}
	
	public List<SHRExternalEncounter> getEncounterUuidList() throws JSONException{
		List<SHRExternalEncounter> encounterList = new ArrayList<SHRExternalEncounter>();
		String clinicCode = Context.getService(SHRActionAuditInfoService.class).getClinicCodeForClinic();
		String url = centralServer + 
				"openmrs/ws/rest/v1/save-Patient/search/patientOriginByOriginName?"
				+ "originName="+clinicCode+"&actionType=encounter";
		String response = HttpUtil.get(url, "", "admin:test");
		JSONArray getEncounterList = new JSONArray(response);
		
		log.error("JSON Encounter List:"+response);
		for(int i = 0; i < getEncounterList.length();i++){
			JSONObject encounterObject = getEncounterList.getJSONObject(i);
//			patientUuidList.add(patientObject.get("patient_uuid").toString());
			SHRExternalEncounter encounter = new SHRExternalEncounter();
			encounter.setEncounterUuid(encounterObject.get("encounterUuid").toString());
			encounter.setPatientUuid(encounterObject.get("patient_uuid").toString());
			encounterList.add(encounter);
		}
		return encounterList;
	}
	
	private String getEncounterInfo(String encounterUuid) throws JSONException{
		
		String url = centralServer+"openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"
				+ encounterUuid + "?includeAll=true";
		log.error("Encounter Get Url: "+url);
		String response = "";
		try{
			response = HttpUtil.get(url, "", "admin:test");
		}catch(Exception e){
			
			return "";
		}
		log.error("Get Encounter Info: "+response);
		JSONObject encounterResponse = new JSONObject(response);
		return encounterResponse.toString();
	}
	private void updateExternalEncounter(String patientUuid,String encounterUuid){
		String externalPatientEncounterUpdateUrl = centralServer + 
				"openmrs/ws/rest/v1/save-Patient/insert/"
				+ "globalExternalPatientEncounter?patient_uuid="+patientUuid+"&encounterUuid="
					+encounterUuid+"&actionStatus=0";
		String get_result = HttpUtil.get(externalPatientEncounterUpdateUrl, "", "admin:test");
		log.error("Update External Encounter: "+get_result);
	}
	private String postEncounterToLocalServer(String postEncounter,String encounterUuid){
		String ret = "";
		
		return ret;
	}
	public void errorLogUpdate(String type,String message, String uuId){
		Context.clearSession();
		Context.openSession();
		SHRActionErrorLog log  = Context.getService(SHRActionErrorLogService.class).getErrorByActionTypeAndIdWithoutSentStatus(type, uuId);
		if(log == null) {
			log = new SHRActionErrorLog();
		}
		log.setAction_type(type);
		log.setError_message(message);
		log.setUuid(uuId);
		Context.getService(SHRActionErrorLogService.class)
			.insertErrorLog(log);
		Context.clearSession();
		Context.openSession();
	}
	
	private boolean 
		createVisit(String visitUuid,String encounterUuid,String patientUuid) 
				throws JSONException{
		String visitFetchUrl = "";
		String vis_global_response = "";
		
		//Central Server Visit Existence Check
		try{
		
			visitFetchUrl = localServer+
					"openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid="+visitUuid;
			vis_global_response = HttpUtil.get(visitFetchUrl, "","admin:test");
			
		}catch(Exception e){
			errorLogUpdate("Encounter Fetch Uuid","Encounter Search Error"+e.toString(),encounterUuid);
			return false;
		}
		
		//Visit Response Parsing
		JSONObject visitFetchJsonObj;
		try {
			 visitFetchJsonObj = new JSONObject(vis_global_response);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			errorLogUpdate("Encounter Fetch Uuid","Encounter Search Error"+e.toString(),encounterUuid);
			return false;
		}
		
		String vis_response = "";
		if(visitFetchJsonObj.get("isFound").toString().contains("false")){
			//Local Server Visit Fetch
			String vis_url =  centralServer+
					"openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid="+visitUuid;
			try{ 
			vis_response = HttpUtil.get(vis_url, "", "admin:test");				
			}catch(Exception e){
				errorLogUpdate("Encounter Fatch Uuid","Encounter Visit Response:"+e.toString()
						,encounterUuid);
				return false;
			}
			
			JSONObject visit_response;
			//Visit Response Parsing
			try{
			 visit_response = new JSONObject(vis_response);
			}catch(Exception e){
				errorLogUpdate("Encounter Fatch Uuid","Encounter Visit Json Parse Error:"+e.toString(),
						encounterUuid);
				return false;
			}
			String createVisit_ = "";
			//Create Visit in Central Server
			Boolean visitFlagError;
			try{ 
				createVisit_ = createVisit(visit_response,patientUuid);
				JSONObject createVisitResponse = new JSONObject(createVisit_);
				visitFlagError = createVisitResponse.get("isSuccessfull").toString().contains("true")
									? false: true;
			}catch(Exception e){
				errorLogUpdate("Encounter Fetch Uuid","Create Visit Error:"+e.toString(),
						encounterUuid);
				return false;
			}
			
		}
		
		return true;
		
	}
		
		private String createVisit(JSONObject obj,String patientUuid) throws JSONException{
			String visitSavingResponse = "";
			obj.remove("isFound");
			obj.put("patient_uuid", patientUuid);
			try {
				
				String visitSavingUrl = localServer + "openmrs/ws/rest/v1/save-Patient/insert/patientVisitDetails";			
				visitSavingResponse = HttpUtil.post(visitSavingUrl, "", obj.toString());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return visitSavingResponse;
		}
		
		private void deleteEncounter(String encounterUuid){
			String deleteWithoutPurge = 
					localServer+"openmrs/ws/rest/v1/encounter/"+encounterUuid;
			String deleteFirst = HttpUtil.delete(deleteWithoutPurge, "", "admin:test");
			//delete encounter
//			errorLogInsert("Error Log Delete ",deleteFirst,encounterUuid,0);
			String deleteUrlString = 
					localServer+"openmrs/ws/rest/v1/encounter/"+encounterUuid
			+"?purge=true";
			String result = HttpUtil.delete(deleteUrlString, "", "admin:test");
			log.error("Delete Encounter: "+result);
		}
		

		private void deleteLocalMoneyReceipt() throws JSONException{
			String E_slipNo = "0";
			try {
				String clinicCode = Context.getService(SHRActionAuditInfoService.class).getClinicCodeForClinic();
				log.error("Clinic Code "+clinicCode);
				String url = centralServer + "openmrs/ws/rest/v1/money-receipt/get-voided-money-receipt/" + Integer.parseInt(clinicCode);
				String moneyReceiptList = HttpUtil.get(url, "", "admin:test");
				org.json.JSONArray getMoneyReceiptList = new org.json.JSONArray(moneyReceiptList);
				
				for(int i = 0; i < getMoneyReceiptList.length();i++){
					org.json.JSONObject mrObject = getMoneyReceiptList.getJSONObject(i);
					String eslipNo = mrObject.get("eslipNo").toString();
					E_slipNo = "";
					E_slipNo = eslipNo;
					
					String eslipUrl = localServer + "openmrs/ws/rest/v1/money-receipt/void-money-receipt-by-eslip/" + eslipNo;
					
					String deletedEslip = HttpUtil.delete(eslipUrl, "", "admin:test");
					if(deletedEslip.equalsIgnoreCase("success")) {
						errorLogUpdate("Money Receipt Delete","success", eslipNo);
						String changeDeleteStatusInGlobal = centralServer + "openmrs/ws/rest/v1/money-receipt/change-delete-status-money-receipt/"+ eslipNo +"/" + 1;
						String changeStatus = HttpUtil.get(changeDeleteStatusInGlobal, "", "admin:test");
						org.json.JSONObject changeStatusJSON = new JSONObject(changeStatus);
						   Boolean status = changeStatusJSON.getBoolean("isSuccessfull");
						   if(!status) {
							   errorLogUpdate("Money Receipt change delete status global","change status Money Receipt Error:"+ changeStatusJSON, eslipNo);
						   }
						   else{
							   log.error("change success in delete money receipt status "+ eslipNo);
							   errorLogUpdate("change success in delete money receipt status global","success", eslipNo);
						   }
					}
					else if(deletedEslip.equalsIgnoreCase("No E-slip Found in server")) {
						log.error("Nothing to delete "+ E_slipNo);
					}
					else {
						errorLogUpdate("Money Receipt Delete","Delete Money Receipt Error:"+ deletedEslip, eslipNo);

					}
					
				}
			} catch (Exception e) {
				errorLogUpdate("Money Receipt Delete","Delete Money Receipt Error:"+ e.toString(),E_slipNo);
			}

		}
}
