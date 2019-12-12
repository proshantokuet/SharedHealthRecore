package org.openmrs.module.sharedhealthrecord.api;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.PersonAddress;
import org.openmrs.module.sharedhealthrecord.domain.PreferredName;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.json.JSONTokener;

import com.google.gson.Gson;

public class ListenerTest extends BaseModuleContextSensitiveTest {
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(SharedHealthRecordService.class));
	}
	String localServer = "http://192.168.19.145/";
	String centralServer="https://192.168.19.147/";
	
//	@Test
//	public void sendPatient() throws ParseException{
//		JSONParser jsonParser = new JSONParser();
////		String last_entry = Context.getService(SHRActionAuditInfoService.class)
////				.getLastEntryForPatient();
////		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
////				.getEventRecords("Patient",last_entry);
//		///openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full
//		
//		String last_entry = "0";
//		List<EventRecordsDTO> records = new ArrayList<EventRecordsDTO>();
//		EventRecordsDTO rec_ = new EventRecordsDTO();
//		rec_.setId(1969);
//		rec_.setUuid("6a0db026-8e12-4837-b446-907e3bf7a590");
//		rec_.setTitle("Patient");
//		rec_.setTimeStamp("2019-08-02 17:32:12");
//		rec_.setUri("");
//		rec_.setObject("/openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full");
//		rec_.setCategory("patient");
//		rec_.setDate_created("2019-08-02 17:32:11");
//		rec_.setTags("patient");
//		records.add(rec_);
//		
//		for(EventRecordsDTO rec: records){
//			String patientUUid = rec.getObject().split("/|\\?")[6];
////			List<SHRExternalPatient> patientsToSend = Context.
////					getService(SHRExternalPatientService.class).
////						findByPatientUuid(patientUUid,"patient");
//			// If patient is not found in table it must be sent
////			if(patientsToSend.size() == 0){				
//				try {
//					patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
////			}
////			else {
////				//If patient is found in table with Is_Send_to_Central = 1, it must be sent
////				if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
////					patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
////				}
////				else {
////					// do nothing
////				}
////			}
//			
//		}
//	}
//	
//	private void patientFetchAndPost(String patientUUid,String id,Boolean failedPatient) throws ParseException, JSONException{
//		
//		JSONParser jsonParser = new JSONParser();
//	
//		// Get Patient Info from Local Server
//		String patientUrl = localServer+"openmrs/ws/rest/v1/patient/"+
//				patientUUid+"?v=full";
//		String patientResponse = HttpUtil.get(patientUrl, "", "admin:test");
//		JSONObject getPatient = new JSONObject(patientResponse);
//		try{
//			
//		
//			String personUuid = (String) getPatient.get("uuid");
//			
//			// Model Conversion for Post into Central Server
//			org.json.simple.JSONObject getPatient_ = (org.json.simple.JSONObject)
//					jsonParser.parse(getPatient.toString());
//			
//			String postData = SharedHealthRecordServiceTest.
//					getPatientObject(getPatient_, personUuid);
//			
//			//Post to Central Server
//			String patientPostUrl = centralServer+
//					"openmrs/ws/rest/v1/bahmnicore/patientprofile";
//			
//			String returnedResult = HttpUtil.post(patientPostUrl, "", postData);
//			System.out.println(returnedResult);
//			
//			// Save last entry in Audit Table
////			if(failedPatient == false){
////				String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
////					.updateAuditPatient(id);
////			}
//			
//		}catch(Exception e){
//			// Error Log Generation on Exception
////			SHRActionErrorLog log = new SHRActionErrorLog();
////			log.setAction_type("Patient");
////			log.setId(Integer.parseInt(id));
////			log.setError_message(e.toString());
////			log.setUuid(patientUUid);
////			Context.getService(SHRActionErrorLogService.class)
////				.insertErrorLog(log);
//			e.printStackTrace();
//		}
//
//	}
	
	@Test
	public void sendEncounter() throws ParseException{
//		String last_entry = Context.getService(SHRActionAuditInfoService.class)
//				.getLastEntryForEncounter();
//		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
//				.getEventRecords("Encounter",last_entry);
	
		String last_entry = "0";
		List<EventRecordsDTO> records = new ArrayList<EventRecordsDTO>();
		EventRecordsDTO rec_ = new EventRecordsDTO();
		rec_.setUuid("c6232470-0248-4886-8fd4-2229b6d77733");
		rec_.setTitle("Encounter");
		rec_.setTimeStamp("2019-10-29 14:51:06");
		rec_.setUri("");
		rec_.setObject("/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/7f9734f4-310a-4804-90c1-da9de1c6a4e9?includeAll=true");
		rec_.setCategory("Encounter");
		rec_.setDate_created("2019-10-29 14:51:05");
		rec_.setTags("Encounter");
		records.add(rec_);
		JSONParser jsonParser = new JSONParser();
		///openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full
		for(EventRecordsDTO rec: records){
			String encounterUUid = rec.getObject().split("/|\\?")[7];
//			List<SHRExternalPatient> patientsToSend = Context.
//					getService(SHRExternalPatientService.class).
//						findByPatientUuid(encounterUUid,"Encounter");
//			if(patientsToSend.size() == 0){
				encounterFetchAndPost(encounterUUid,Integer.toString(rec.getId()),false);				
//			}
//			else {
//				if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
//					encounterFetchAndPost(encounterUUid,Integer.toString(rec.getId()),false);
//				}
//				else {
//					// do nothing
//				}
//			}
			
		}
	}
	
	private void encounterFetchAndPost(String encounterUuid, String id,Boolean failedEncounter) throws ParseException{
		JSONParser jsonParser = new JSONParser();
			
			
			Boolean status = false;
			try{
				String getUrl = localServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"
						+ encounterUuid + "?includeAll=true";
				String response = HttpUtil.get(getUrl, "", "admin:test");
				JSONObject encounterResponse = new JSONObject(response);
				String postUrl = centralServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
				
				String postResponse = HttpUtil.post(postUrl, "", encounterResponse.toString());
				System.out.println(postResponse);
//				if(failedEncounter == false){
//					Context.getService(SHRActionAuditInfoService.class)
//						.updateAuditEncounter(id);					
//					
//				}
			}catch(Exception e){
//				SHRActionErrorLog log = new SHRActionErrorLog();
//				log.setAction_type("Encounter");
//				log.setId(Integer.parseInt(id));
//				log.setError_message(e.toString());
//				log.setUuid(encounterUuid);
//				Context.getService(SHRActionErrorLogService.class)
//					.insertErrorLog(log);
			}		
		}


}
