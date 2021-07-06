package org.openmrs.module.sharedhealthrecord.web.listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.UBSDataExtract;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SharedHealthRecordService;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.module.sharedhealthrecord.utils.ServerAddress;
import org.openmrs.module.sharedhealthrecord.web.controller.rest.SharedHealthRecordManageRestController;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@EnableScheduling
@Configuration
@EnableAsync
@Controller
public class UBSExtractServiceANdSaveListener{
	
	String localServer = ServerAddress.localServer();
	String centralServer = ServerAddress.centralServer();
	String isDeployInGlobal = ServerAddress.isDeployInGlobal;
	public static DateFormat dateFormatTwentyFourHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final ReentrantLock lock = new ReentrantLock();
	Gson gson = new Gson();

	@SuppressWarnings("rawtypes")
//	@Scheduled(fixedRate=10000)
	private static final Logger log = LoggerFactory.getLogger(UBSExtractServiceANdSaveListener.class);
	public void sendAllData() throws Exception {
		log.error("Entered in followup listener" + new Date());
		if (!lock.tryLock()) {
			log.error("It is already in progress.");
	        return;
		}
		log.error("isDeployInGlobal " + isDeployInGlobal);

			Context.openSession();
			JSONParser jsonParser = new JSONParser();
			JSONObject getResponse = null;
			boolean status = true;
			try{
				String globalServerUrl = centralServer + "openmrs/ws/rest/v1/visittype";
				String get_result = HttpUtil.get(globalServerUrl, "", "admin:test");
				JSONObject patienResponseCheck = (JSONObject) jsonParser.parse(get_result);			
			}catch(Exception e){
				e.printStackTrace();
				status = false;
			}
			
			if(status){
				try {
					extractAndSave();
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					lock.unlock();
					log.error("complete listener ubs extraction at:" +new Date());
				}
			}
			
			Context.closeSession();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void extractAndSave() {
		
		
		String last_entry = Context.getService(SHRActionAuditInfoService.class).getLastEntryByType("ExtractID");
		List<EventRecordsDTO> records = Context.getService(SHRActionAuditInfoService.class).getEventRecords("Encounter",last_entry);
		JSONParser jsonParser = new JSONParser();
		try {   
		
			for (EventRecordsDTO eventRecordsDTO : records) {
				
				String encounterUUid = eventRecordsDTO.getObject().split("/|\\?")[7];
				String getEncounterUrl = localServer +"openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"+ encounterUUid +"?includeAll=false";
				//String url = "/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/581753b1-065b-44da-b7c6-3f656f997044?includeAll=true";
				//String getEncounterUrl = centralServer + url;
				String patientencounterResponse = HttpUtil.get(getEncounterUrl, "", "superman:Admin123");
				JSONObject EncounterObj;
	
				EncounterObj = (JSONObject) jsonParser.parse(patientencounterResponse);
				String patientUuid = (String)EncounterObj.get("patientUuid");
	
				JSONArray obs = (JSONArray) EncounterObj.get("observations");
	
				JSONArray IntialJsonDHISArray =  getUBSObservations(obs);
				
		        if(IntialJsonDHISArray.size() > 0) {
					String serviceString = IntialJsonDHISArray.toString();
					log.error("serviceString" + serviceString);
					//System.out.println(serviceString);
					Object document = UBSConfigurationJsonPath.parseDocument(serviceString);
					List<String> servicesInObservation = JsonPath.read(document, "$..service");
					log.error("servicesInObservation" + servicesInObservation.toString());
					Set<String> uniqueSetOfServices = new HashSet<>();
					uniqueSetOfServices.addAll(servicesInObservation);
					uniqueSetOfServices.forEach(uniqueSetOfService ->{
						List<String> extractServiceJSON = JsonPath.read(document, "$.[?(@.service == '"+uniqueSetOfService+ "' && @.isVoided == false)]");
					    String jsonStr = JSONArray.toJSONString(extractServiceJSON);
							try {
								JSONArray extractServiceArray = (JSONArray) jsonParser.parse(jsonStr);
								String tableName = UBSTABLE_MAP.get(uniqueSetOfService);
								if(!StringUtils.isBlank(tableName)) {
									boolean status = Context.getService(SharedHealthRecordService.class).deleteExtractedFieldsByEncounterUuid(encounterUUid, tableName);
									log.error("status in listner" + status);
									extractServiceArray.forEach(service -> {
										JSONObject serviceObject = (JSONObject) service;
										serviceObject.put("patientUuid", patientUuid);
										serviceObject.put("encounterUuid", encounterUUid);
									});
	
									 List<UBSDataExtract> data = gson.fromJson(extractServiceArray.toString(),
										    new TypeToken<ArrayList<UBSDataExtract>>() {}.getType());
									 log.error("data size" + data.size());
									boolean flag = true;
									for (UBSDataExtract obsData : data) {
										flag = Context.getService(SharedHealthRecordService.class).ubsSaveExtractedFieldsToTable(obsData, tableName);
										if(!flag) {
											throw new RuntimeException();
										}
									}
								}
							} catch (ParseException e) {
								e.printStackTrace();
								throw new RuntimeException();
							}
					});

					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
						.updateAuditInfoByType(Integer.toString(eventRecordsDTO.getId()), "ExtractID");
		        }
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	@SuppressWarnings("unchecked")
	public static JSONArray getUBSObservations(JSONArray _obs) {
		JSONParser jsonParser = new JSONParser();
		JSONArray observations = new JSONArray();
		
		_obs.forEach(_ob -> {
			JSONObject ob = (JSONObject) _ob;
			//System.out.println(ob.toString());
			String type = (String) ob.get("type");
			JSONArray groupMembers = (JSONArray) ob.get("groupMembers");
			// System.out.println("Coded..........:" + groupMembers.size() + " type:" + type);
			try {
				if (!StringUtils.isBlank(type) && type.equalsIgnoreCase("Coded")) {
					JSONObject codedConceptJsonObject = new JSONObject();
					String serviceName = (String) ob.get("formFieldPath");
					String serviceSplit[] = serviceName.split("\\.");
					serviceName = serviceSplit[0];
					JSONObject conceptJsonObject = (JSONObject) ob.get("concept");
					String questionName = (String) conceptJsonObject.get("name");
					String answerValue = (String) ob.get("valueAsString");
					//String voidreason = (String) ob.get("voidReason");
					boolean isVoided = (boolean) ob.get("voided");
					codedConceptJsonObject.put("question", questionName);
					//codedConceptJsonObject.put("voidReason", voidreason);
					codedConceptJsonObject.put("isVoided", isVoided);
					codedConceptJsonObject.put("answer", answerValue);
					codedConceptJsonObject.put("service", serviceName);
					observations.add(codedConceptJsonObject);

				} else if (groupMembers.size() != 0) {
					groupMembers.forEach(_groupMember -> {
						JSONObject groupMember = (JSONObject) _groupMember; 
						JSONObject codedConceptJsonObject = new JSONObject();
						String serviceName = (String) groupMember.get("formFieldPath");
						String serviceSplit[] = serviceName.split("\\.");
						serviceName = serviceSplit[0];
						JSONObject conceptJsonObject = (JSONObject) groupMember.get("concept");
						String questionName = (String) conceptJsonObject.get("name");
						String answerValue = (String) groupMember.get("valueAsString");
						//String voidreason = (String) ob.get("voidReason");
						boolean isVoided = (boolean) ob.get("voided");
						codedConceptJsonObject.put("question", questionName);
						//codedConceptJsonObject.put("voidReason", voidreason);
						codedConceptJsonObject.put("isVoided", isVoided);
						codedConceptJsonObject.put("answer", answerValue);
						codedConceptJsonObject.put("service", serviceName);
						observations.add(codedConceptJsonObject);
					});
					
				} else {
					JSONObject codedConceptJsonObject = new JSONObject();
					String serviceName = (String) ob.get("formFieldPath");
					String serviceSplit[] = serviceName.split("\\.");
					serviceName = serviceSplit[0];
					JSONObject conceptJsonObject = (JSONObject) ob.get("concept");
					String questionName = (String) conceptJsonObject.get("name");
					String answerValue = (String) ob.get("valueAsString");
					//String voidreason = (String) ob.get("voidReason");
					boolean isVoided = (boolean) ob.get("voided");
					codedConceptJsonObject.put("question", questionName);
					//codedConceptJsonObject.put("voidReason", voidreason);
					codedConceptJsonObject.put("isVoided", isVoided);

					codedConceptJsonObject.put("answer", answerValue);
					codedConceptJsonObject.put("service", serviceName);
					observations.add(codedConceptJsonObject);
				}
				
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				//System.out.println(ob);
				e.printStackTrace();
			}
		});
		return observations;
	}
	
	public static final Map<String, String> UBSTABLE_MAP = new HashMap<String, String>();
	static {

		UBSTABLE_MAP.put("Acute Health Condition", "ubs_report_acute_health_condition");
		UBSTABLE_MAP.put("Sexual & Reproductive health (SRH)", "ubs_report_sexual_reproductive_health");
		UBSTABLE_MAP.put("Non Communicable and other Chronic Disease", "ubs_report_non_communicable_dieases");
		UBSTABLE_MAP.put("Injuries", "ubs_report_injuries");
		UBSTABLE_MAP.put("Nutrition", "ubs_report_nutrition");
		UBSTABLE_MAP.put("Mental Health", "ubs_report_mental_health");
		UBSTABLE_MAP.put("Communicable Disease", "ubs_report_communicable_disease");
		UBSTABLE_MAP.put("Referrals", "ubs_report_referrals");
		UBSTABLE_MAP.put("Admission", "ubs_report_ipd_admission");
		UBSTABLE_MAP.put("Discharge", "ubs_report_ipd_discharge");
		UBSTABLE_MAP.put("Death", "ubs_report_mortality");
		UBSTABLE_MAP.put("Child Vaccination Form", "ubs_report_child_vaccination");
		UBSTABLE_MAP.put("Delivery Service", "ubs_report_delivery_service");
		
	}

}
