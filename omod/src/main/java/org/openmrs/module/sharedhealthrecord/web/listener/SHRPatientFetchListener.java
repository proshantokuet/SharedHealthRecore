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
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.module.sharedhealthrecord.utils.ServerAddress;
import org.openmrs.module.sharedhealthrecord.web.controller.rest.SharedHealthRecordManageRestController;
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
public class SHRPatientFetchListener {
	String localServer = ServerAddress.localServer();
	String centralServer = ServerAddress.centralServer();
	
	public void fetchAndUpdatePatient(){
		Context.openSession();
//		errorLogUpdate("Patient Fetch Problem","Hitting in Patient Fetch",UUID.randomUUID().toString());
		try{
			patientFetchAndUpdateExecute();
		}catch(Exception e){
			errorLogUpdate("Patient Fetch Problem",e.toString(),UUID.randomUUID().toString());
		}
		try{
			encounterFetchAndUpdateExecute();
		}catch(Exception e){
			
		}
		Context.closeSession();
	}
	
	public void patientFetchAndUpdateExecute(){
		//fetch patient 
		List<String> patientUuidList = new ArrayList<String>();
		String postPatientResponse = "";
		//Getting the changed local patient which is in the global server
		try{
			patientUuidList = getPatientUuidList();
		}catch(Exception e){
			errorLogUpdate("Patient Uuid Fetch", e.toString(),UUID.randomUUID().toString());
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
				return;
			}
			
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
		List<String> patientUuidList = new ArrayList<String>();
		
		String url = centralServer + 
				"openmrs/ws/rest/v1/save-Patient/search/patientOriginByOriginName?"
				+ "originName="+localServer;
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
	
	public void encounterFetchAndUpdateExecute(){
		List<String> encounterUuidList = new ArrayList<String>();
		String postEncounterResponse = "";
		try{
			encounterUuidList = getEncounterUuidList();
		}catch(Exception e){
			errorLogUpdate("Encounter Uuid Fetch", e.toString(),UUID.randomUUID().toString());
		}
		
		for(String encounterUuid: encounterUuidList){
			String encounter = "";
			try{
				encounter = getEncounterInfo(encounterUuid);
			}catch(Exception e){
				errorLogUpdate("Encounter Fetch Uuid",e.toString(),encounterUuid);
				return;
			}
			try{
				 postEncounterResponse = postEncounterToLocalServer(encounter,encounterUuid);
				 JSONObject response = new JSONObject(postEncounterResponse);
				 //If condition on response
				 updateExternalEncounter(encounterUuid);
				 
			}catch(Exception e){
				
			}
		}
	}
	
	public List<String> getEncounterUuidList(){
		List<String> encounterUuidList = new ArrayList<String>();
		
		return encounterUuidList;
	}
	
	private String getEncounterInfo(String encounterUuid){
		String ret = "";
		
		return ret;
	}
	private void updateExternalEncounter(String encounterUuid){
		
	}
	private String postEncounterToLocalServer(String postEncounter,String encounterUuid){
		String ret = "";
		
		return ret;
	}
	public void errorLogUpdate(String type,String message, String uuId){
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
}
