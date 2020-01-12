package org.openmrs.module.sharedhealthrecord.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;

public abstract class PatientSendProcess {
	int patientState;
	String patientResponse;
	String postPatientFormatData;
	String postPatientUrl;
	String centralServerPatientCheckResponse;
	JSONObject patienResponseCheck;
	private enum sendState{
		NOT_SENT,
		LOCAL_PATIENT_SENT,
		EXTERNAL_PATIENT_SENT,
		FAILED_PATIENT_SENT
	}
	public void sendPatient(PatientSendModel patientInfo,PatientCondition condition) throws JSONException, ParseException{
		patientState = patientSendState(condition);
		
		if(patientState == sendState.NOT_SENT.ordinal()) return;
		else if(patientState == sendState.EXTERNAL_PATIENT_SENT.ordinal()){
			Boolean updateStatus = updateExternalPatientInGlobalServer(patientInfo);
			if(updateStatus == false) return;
		}
		else if(patientState == sendState.FAILED_PATIENT_SENT.ordinal())
			patientInfo.setVoidedStatus(patientInfo.getVoidedStatus() + 1);
		
		if(!getPatient(patientInfo)) return;
		
		if(!preparePostPatientData(patientInfo)) return;
		
		if(!postPatient(patientInfo)) return;
		
		if(patientState == sendState.FAILED_PATIENT_SENT.ordinal()){
			updateErrorLog(patientInfo);
		}
			
		if(patientState != sendState.FAILED_PATIENT_SENT.ordinal()){
			updateLastEntry(patientInfo.getEvent_records_id());
		}
		
		
	}
	
	public abstract int patientSendState(PatientCondition condition);
	
	public Boolean getPatient(PatientSendModel patientInfo){
		String patientUrl = patientInfo.getLocalServer()+"openmrs/ws/rest/v1/patient/"+
				patientInfo.getPatientUuid()+"?v=full";

		patientResponse = "";
		try{
			patientResponse = HttpUtil.get(patientUrl, "", "admin:test");								
		}catch(Exception e){
			ListenerUtil.errorLogInsert("Patient","Patient Get Error"+e.toString(),patientInfo.getPatientUuid(),patientInfo.getVoidedStatus());
			return false;
		}
		return true;
	}
	
	
	
	
	public Boolean updateExternalPatientInGlobalServer(PatientSendModel patientInfo){
		String externalPatientUpdateUrl = patientInfo.getCentralServer() + 
				"openmrs/ws/rest/v1/save-Patient/insert/"
				+ "externalPatient?patient_uuid="
					+patientInfo.getPatientUuid()+"&action_status=1";
		String get_result = "";
		try{
			get_result = HttpUtil.get(externalPatientUpdateUrl, "", "admin:test");
			
		}catch(Exception e){
			//errorLogInsert(Type,error,uid,voidedStatus)
			ListenerUtil.errorLogInsert("Patient",e.toString(),patientInfo.getPatientUuid(),0);
			return false;
		}
		return true;
	}
	
	public Boolean preparePostPatientData(PatientSendModel patientInfo) throws JSONException, ParseException{
		JSONObject getPatient;
		JSONParser jsonParser = new JSONParser();
		try {
			 getPatient = new JSONObject(patientResponse);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			ListenerUtil.errorLogInsert("Patient",e.toString(),patientInfo.getPatientUuid(),0);
			return false;
		}
		String personUuid = (String) getPatient.get("uuid");
		JSONObject getPatient_ = 
				(JSONObject) jsonParser.parse(getPatient.toString());
		
		 postPatientFormatData = ListenerUtil.
				getPatientObject(getPatient_, personUuid,patientInfo.getCentralServer());
		return true;
	}
	public Boolean preparePostUrl(PatientSendModel patientInfo) throws JSONException{
		postPatientUrl = patientInfo.getCentralServer()+
				"openmrs/ws/rest/v1/bahmnicore/patientprofile";
		String centralServerPatientCheckUrl = patientInfo.getCentralServer()+"openmrs/ws/rest/v1/patient/"+
				patientInfo.getPatientUuid()+"?v=full";
		 centralServerPatientCheckResponse = "";
		try{
			centralServerPatientCheckResponse = HttpUtil.get(centralServerPatientCheckUrl,
						"", "admin:test");
		}catch(Exception e){
			ListenerUtil.errorLogInsert("Patient","Server Patient Check: "+ e.toString(),patientInfo.getPatientUuid(),
					patientInfo.getVoidedStatus());
			return false;
		}
		
		 patienResponseCheck = new JSONObject(centralServerPatientCheckResponse);
		//If Error No String concat as Add Action - Else Update action API
		postPatientUrl += patienResponseCheck.has("error") ? "" : "/"+patientInfo.getPatientUuid();
		
		return true;
	}
		
	public Boolean postPatient(PatientSendModel patientInfo) throws JSONException{
		String returnedResult = "";
		try{ 
			
			returnedResult = HttpUtil.post(postPatientUrl, "", postPatientFormatData);
//			errorLogUpdate("patient post",returnedResult,patientUUid);
			//origin table will be inserted in global server for addition only
			if(patienResponseCheck.has("error")){
				String insertUrl = patientInfo.getCentralServer()+"openmrs/ws/rest/v1/save-Patient/insert/patientOriginDetails";
					insertUrl += "?patient_uuid="+patientInfo.getPatientUuid()+"&patient_origin="+
				patientInfo.getLocalServer()+"/";
					
				String get = "";
				try{
					get = HttpUtil.get(insertUrl, "", "admin:test");
				}catch(Exception e){
					ListenerUtil.errorLogInsert("Patient","Local Server Save Info Error:" + e.toString()
							,patientInfo.getPatientUuid(),patientInfo.getVoidedStatus());
					return false;
				}
			}
		
		}catch(Exception e){
			ListenerUtil.errorLogInsert("Patient",e.toString(),
					patientInfo.getPatientUuid(),patientInfo.getVoidedStatus());
			return false;
		}
		return true;
	}
	
	public void updateErrorLog(PatientSendModel patientInfo){
		Context.openSession();
		try{
			
			Context.getService(SHRActionErrorLogService.class)
			.updateSentStatus(patientInfo.getEid(), 1);
			
		}catch(Exception e){
			
		}
		
		Context.closeSession();
	}
	
	public void updateLastEntry(String event_records_id){
		Context.openSession();
		Context.getService(SHRActionAuditInfoService.class)
		.updateAuditPatient(event_records_id);
		Context.closeSession();
	}
}
