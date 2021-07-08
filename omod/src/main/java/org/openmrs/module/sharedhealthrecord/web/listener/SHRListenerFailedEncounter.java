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
import org.openmrs.module.sharedhealthrecord.api.SharedHealthRecordService;
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
	
	
	public synchronized void sendFailedEncounter(){
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
						Context.getService(SHRActionErrorLogService.class).updateSentStatus(encounter.getEid(), flag == true ? 1 :0);
					} catch (ParseException e) {
						if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
							errorLogInsert("Encounter","Encounter Error",encounter.getUuid(),encounter.getVoided() == 2 ? 1 : encounter.getVoided(),"");
						}
						else {
							errorLogInsert("Encounter","Encounter Error",encounter.getUuid(),encounter.getVoided(),"");
						}
						e.printStackTrace();
					}
				}
			}
			
		}
	}
	
	
	@SuppressWarnings("unchecked")
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
						errorLogInsert("Encounter","Encounter get Error:"+response,encounterUuid,voidedStatus == 2 ? 1 : voidedStatus,"");
					}
					else {
						errorLogInsert("Encounter","Encounter get Error:"+response,encounterUuid,voidedStatus,"");
					}					
					return false;
				}
				
				//Encounter Response Formatting
				//JSONObject encounterResponse = new JSONObject(response);
				org.json.simple.JSONObject enc_response = new org.json.simple.JSONObject();
				try{
				 enc_response = (org.json.simple.JSONObject) jsonParser.parse(response);
				}catch(Exception e){
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Encounter",e.toString(),encounterUuid,voidedStatus == 2 ? 1 : voidedStatus,"");
					}
					else {
						errorLogInsert("Encounter",e.toString(),encounterUuid,voidedStatus,"");
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
						errorLogInsert("Encounter","Encounter Search Error"+e.toString(),encounterUuid,voidedStatus == 2 ? 1 : voidedStatus,"");
					}
					else {
						errorLogInsert("Encounter","Encounter Search Error"+e.toString(),encounterUuid,voidedStatus,"");
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
									,encounterUuid,voidedStatus == 2 ? 1 : voidedStatus,"");
						}
						else {
							errorLogInsert("Encounter","Encounter Visit Response:"+e.toString()
									,encounterUuid,voidedStatus,"");
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
									encounterUuid,voidedStatus == 2 ? 1 : voidedStatus,"");
						}
						else {
							errorLogInsert("Encounter","Encounter Visit Json Parse Error:"+e.toString(),
									encounterUuid,voidedStatus,"");
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
									encounterUuid,voidedStatus == 2 ? 1 : voidedStatus,"");
						}
						else {
							errorLogInsert("Encounter","Create Visit Error:"+e.toString(),
									encounterUuid,voidedStatus,"");
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
							encounterUuid,voidedStatus,"");
					return false;
				}
				

				// Encounter Post JSON Format Preparation
				String visitTypeValue = EncounterDataConverter.visitTypeMapping.get(enc_response.get("visitTypeUuid").toString());
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
						String ProviderInJson = (String) providerObject.get("uuid");
						boolean isProviderlabTechnician = Context.getService(SharedHealthRecordService.class).checkIsProviderIsLabTechnicin(ProviderInJson);
						String providerUuid = ""; 
						if(isProviderlabTechnician) {
							providerUuid = "7d162c29-3f12-11e4-adec-0800271c1b75";
						} else {
							providerUuid = "c1c26908-3f10-11e4-adec-0800271c1b75";
						}
						providerObject.remove("uuid");
						providerObject.put("uuid", providerUuid);
					}
				}
				
				//Encounter Post
				String postUrl = centralServer + "openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
				
				//Finding Add or Update Action for Post Url
				
				org.json.simple.JSONArray obs = EncounterDataConverter.getObservations((org.json.simple.JSONArray)enc_response.get("observations"));
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
								encounterUuid,voidedStatus == 2 ? 1 : voidedStatus,encounter.toJSONString());
					}
					else {
						errorLogInsert("Encounter","Encounter Global Search error:"+e.toString(),
								encounterUuid,voidedStatus,encounter.toJSONString());
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
						errorLogInsert("Encounter",errorMessage,encounterUuid,voidedStatus,encounter.toJSONString());
						postStatus = false;
					}
					if(!postStatus) {
						return false;
					}
	
				}catch(Exception e){
					if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Encounter","Encounter post error:"+e.toString(),
								encounterUuid,voidedStatus == 2 ? 1 : voidedStatus,encounter.toJSONString());
					}
					else {
						errorLogInsert("Encounter","Encounter post error:"+e.toString(),
								encounterUuid,voidedStatus,encounter.toJSONString());
					}
					return false;
				}

			}catch(Exception e){
				if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
						errorLogInsert("Encounter","Encounter Error:"+e.toString(),encounterUuid,voidedStatus == 2 ? 1 : voidedStatus,"");
					}
					else {
						errorLogInsert("Encounter","Encounter Error:"+e.toString(),encounterUuid,voidedStatus,"");
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
	
	public void errorLogInsert(String action_type,String message,String uuId,Integer voided, String postJson){
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
	
}
