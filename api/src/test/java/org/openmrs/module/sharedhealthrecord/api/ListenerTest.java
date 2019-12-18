package org.openmrs.module.sharedhealthrecord.api;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.domain.Encounter;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;
import org.openmrs.module.sharedhealthrecord.domain.PersonAddress;
import org.openmrs.module.sharedhealthrecord.domain.PreferredName;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.json.JSONTokener;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class ListenerTest extends BaseModuleContextSensitiveTest {
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(SharedHealthRecordService.class));
	}
	String localServer = "http://192.168.19.145/";
	String centralServer="https://192.168.19.147/";
	
	@Test
	public void sendPatient() throws ParseException, JSONException{
		JSONParser jsonParser = new JSONParser();
//		String last_entry = Context.getService(SHRActionAuditInfoService.class)
//				.getLastEntryForPatient();
//		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
//				.getEventRecords("Patient",last_entry);
		///openmrs/ws/rest/v1/patient/d8b039a9-1dd3-46df-8571-cddeca6c092b?v=full
		
		String last_entry = "0";
		List<EventRecordsDTO> records = new ArrayList<EventRecordsDTO>();
		EventRecordsDTO rec_ = new EventRecordsDTO();
//		rec_.setId(48512);
		rec_.setUuid("2aef663c-a947-44e2-96b3-b58f1ab6e53d");
		rec_.setTitle("Patient");
		rec_.setTimestamp("2019-12-12 19:40:38");
		rec_.setUri("");
		rec_.setObject("/openmrs/ws/rest/v1/patient/2eaf3f42-f320-4633-8af4-97268539f0f0?v=full");
		rec_.setCategory("patient");
		rec_.setDate_created("2019-12-12 19:40:37");
		rec_.setTags("patient");
		records.add(rec_);
		
		for(EventRecordsDTO rec: records){
			String patientUUid = rec.getObject().split("/|\\?")[6];
//			List<SHRExternalPatient> patientsToSend = Context.
//					getService(SHRExternalPatientService.class).
//						findByPatientUuid(patientUUid,"patient");
			// If patient is not found in table it must be sent
//			if(patientsToSend.size() == 0){				
//				try {
//					patientFetchAndPost(patientUUid,Integer.toString(rec.getId()),false);
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
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
	
	private void patientFetchAndPost(String patientUUid,String id,Boolean failedPatient) throws ParseException, JSONException{
		
		JSONParser jsonParser = new JSONParser();
	
		// Get Patient Info from Local Server
		String patientUrl = localServer+"openmrs/ws/rest/v1/patient/"+
				patientUUid+"?v=full";
		String patientResponse = HttpUtil.get(patientUrl, "", "admin:test");
		JSONObject getPatient = new JSONObject(patientResponse);
		try{
			
		
			String personUuid = (String) getPatient.get("uuid");
			
			// Model Conversion for Post into Central Server
			org.json.simple.JSONObject getPatient_ = (org.json.simple.JSONObject)
					jsonParser.parse(getPatient.toString());
			
			String postData = SharedHealthRecordServiceTest.
					getPatientObject(getPatient_, personUuid);
			
			//Post to Central Server
			String patientPostUrl = centralServer+
					"openmrs/ws/rest/v1/bahmnicore/patientprofile";
			
			String returnedResult = HttpUtil.post(patientPostUrl, "", postData);
			System.out.println(returnedResult);
			
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
	
	@Test
	public void sendEncounter() throws ParseException{
//		String last_entry = Context.getService(SHRActionAuditInfoService.class)
//				.getLastEntryForEncounter();
//		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class)
//				.getEventRecords("Encounter",last_entry);
	
		String last_entry = "0";
		List<EventRecordsDTO> records = new ArrayList<EventRecordsDTO>();
		EventRecordsDTO rec_ = new EventRecordsDTO();
		rec_.setUuid("7bc54e4a-9b3d-43e7-a956-3c05e32ae00e");
		rec_.setTitle("Encounter");
		rec_.setTimestamp("2019-12-12 19:44:22");
		rec_.setUri("");
		rec_.setObject("/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/7c791340-6951-4ac3-9d29-6d85be11aa0a?includeAll=true");
		rec_.setCategory("Encounter");
		rec_.setDate_created("2019-12-12 19:44:22");
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
//				encounterFetchAndPost(encounterUUid,Integer.toString(rec.getId()),false);				
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
				org.json.simple.JSONObject enc_response = (org.json.simple.JSONObject) jsonParser.
						parse(encounterResponse.toString());
				String visitUuid = ObservationServiceTest.createVisit(enc_response,"c5854fd7-3f12-11e4-adec-0800271c1b75");
				
				org.json.simple.JSONObject visitObj = (org.json.simple.JSONObject)
						jsonParser.parse(visitUuid);
				enc_response.remove("visitUuid");
				enc_response.put("visitUuid", visitObj.get("uuid"));
				
				String visitTypeValue =ObservationServiceTest.visitTypeMapping.get(enc_response.get("visitTypeUuid").toString());
				enc_response.remove("visitTypeUuid");
				enc_response.put("visitType", visitTypeValue);
				
				org.json.simple.JSONArray obs = ObservationServiceTest.getObservations((org.json.simple.JSONArray)enc_response.get("observations"));
				enc_response.remove("observations");
				enc_response.put("observations", obs);
				
				if(enc_response.containsKey("locationUuid"))
				{
					enc_response.remove("locationUuid");
				}
				if(enc_response.containsKey("location"))
					enc_response.remove("location");
				
				enc_response.put("location", "c5854fd7-3f12-11e4-adec-0800271c1b75");
				
				String postUrl = centralServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
				
				org.json.simple.JSONObject encounter = (org.json.simple.JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(enc_response.toString(),Encounter.class)));
				
				String postResponse = HttpUtil.post(postUrl, "", encounter.toJSONString());
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
	
	@Test
	public void sendMoneyReceipt(){
		JSONParser jsonParser = new JSONParser();
		// Check shr_action_audit_info for last sent timestamp
//		String timestamp = Context.getService(SHRActionAuditInfoService.class)
//				.getLastEntryForMoneyReceipt();
		
		// iterate Money receipt
		try{
//			List<MoneyReceiptDTO> receipts = Context.
//				getService(SHRActionAuditInfoService.class)
//				.getMoneyReceipt(timestamp);
			List<MoneyReceiptDTO> receipts = new ArrayList<MoneyReceiptDTO>();
			MoneyReceiptDTO receipt_ = new MoneyReceiptDTO();
			String mid_ = "42292";
//			MoneyReceiptFetchAndPost(mid_,true);
			for(MoneyReceiptDTO receipt: receipts){
					//Local Money Receipt update
				String mid = Integer.toString(receipt.getMid());
				
//				MoneyReceiptFetchAndPost(mid,false);
			}
		}catch(Exception e){
			
		}
		// catch will enter the data into shr_action_error_log table
		
		
		
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
//					String timestampUpdate = 	Context.getService(SHRActionAuditInfoService.class)
//				.updateAuditMoneyReceipt(timestamp);
				}
			}
			else {
				//ELSE failed action
				SHRActionErrorLog log = new SHRActionErrorLog();
				log.setAction_type("Money Receipt");
				log.setId(Integer.parseInt(mid));
				log.setError_message(postAction);
				log.setUuid(UUID.randomUUID().toString());
				Context.getService(SHRActionErrorLogService.class)
					.insertErrorLog(log);
			}
		}catch(Exception e){
			SHRActionErrorLog log = new SHRActionErrorLog();
			log.setAction_type("Money Receipt");
			log.setId(Integer.parseInt(mid));
			log.setError_message(e.toString());
			log.setUuid(UUID.randomUUID().toString());
			Context.getService(SHRActionErrorLogService.class)
				.insertErrorLog(log);
		}
	}

	

}
