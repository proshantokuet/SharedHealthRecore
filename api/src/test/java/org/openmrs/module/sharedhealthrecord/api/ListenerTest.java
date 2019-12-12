package org.openmrs.module.sharedhealthrecord.api;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.simple.JSONArray;
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


import com.google.gson.Gson;

public class ListenerTest extends BaseModuleContextSensitiveTest {
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(SharedHealthRecordService.class));
	}
	String localServer = "http://192.168.147.10/";
	String centralServer="http://192.168.33.10/";
	
	@Test
	public void sendPatient() throws ParseException{
		JSONParser jsonParser = new JSONParser();
//		String last_entry = Context.getService(SHRActionAuditInfoService.class)
//				.getLastEntryForPatient();
//		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
//				.getEventRecords("Patient",last_entry);
		///openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full
		
		String last_entry = "0";
		List<EventRecordsDTO> records = new ArrayList<EventRecordsDTO>();
		EventRecordsDTO rec_ = new EventRecordsDTO();
		rec_.setId(1969);
		rec_.setUuid("6a0db026-8e12-4837-b446-907e3bf7a590");
		rec_.setTitle("Patient");
		rec_.setTimeStamp("2019-08-02 17:32:12");
		rec_.setUri("");
		rec_.setObject("/openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full");
		rec_.setCategory("patient");
		rec_.setDate_created("2019-08-02 17:32:11");
		rec_.setTags("patient");
		records.add(rec_);
		
		for(EventRecordsDTO rec: records){
			String patientUUid = rec.getObject().split("/|\\?")[6];
//			List<SHRExternalPatient> patientsToSend = Context.
//					getService(SHRExternalPatientService.class).
//						findByPatientUuid(patientUUid,"patient");
			// If patient is not found in table it must be sent
//			if(patientsToSend.size() == 0){				
				patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
				
//			}
//			else {
//				//If patient is found in table with Is_Send_to_Central = 1, it must be sent
//				if(patientsToSend.get(0).getIs_send_to_central().contains("1")){
//					patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
//				}
//				else {
//					// do nothing
//				}
//			}
			
		}
	}
	
	private void patientFetchAndPost(String patientUUid,String id,Boolean failedPatient) throws ParseException{
		
		JSONParser jsonParser = new JSONParser();
	
		// Get Patient Info from Local Server
		String patientUrl = localServer+"openmrs/ws/rest/v1/patient/"+
				"d8b039a9-1dd3-46df-8571-cddeca6c092b"+"?v=full";
		String patientResponse = HttpUtil.get(patientUrl, "", "admin:test");
		JSONObject getPatient = (JSONObject) jsonParser.parse(patientResponse);
		try{
			
		
			String personUuid = (String) getPatient.get("uuid");
			
			// Model Conversion for Post into Central Server
			org.json.simple.JSONObject getPatient_ = (org.json.simple.JSONObject)
					jsonParser.parse(getPatient.toString());
			
			String postData = SharedHealthRecordServiceTest.
					getPatientObject(getPatient_, personUuid);
			
			//Post to Central Server
			String patientPostUrl = centralServer+
					"/openmrs/ws/rest/v1/bahmnicore/patientprofile";
			
			String returnedResult = HttpUtil.post(patientPostUrl, "", postData);
			
			// Save last entry in Audit Table
//			if(failedPatient == false){
//				String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
//					.updateAuditPatient(id);
//			}
			
		}catch(Exception e){
			// Error Log Generation on Exception
//			SHRActionErrorLog log = new SHRActionErrorLog();
//			log.setAction_type("Patient");
//			log.setId(Integer.parseInt(id));
//			log.setError_message(e.toString());
//			log.setUuid(patientUUid);
//			Context.getService(SHRActionErrorLogService.class)
//				.insertErrorLog(log);
			e.printStackTrace();
		}

	}

}
