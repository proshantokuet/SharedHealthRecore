/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.sharedhealthrecord.api;

import static org.junit.Assert.assertNotNull;
import groovy.ui.Console;

import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.domain.Encounter;
import org.openmrs.module.sharedhealthrecord.domain.Observation;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithGroupMemebrs;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithValues;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.web.servlet.result.PrintingResultHandler;

import ca.uhn.hl7v2.model.v25.segment.IIM;

import com.google.gson.Gson;import com.google.gson.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;


/**
 * Tests {@link $ SharedHealthRecordService} .
 */
public class ObservationServiceTest extends BaseModuleContextSensitiveTest {
	
	private final static String baseOpenmrsUrl = "https://192.168.19.145";
	
	private final static String globalServerUrl = "https://bahmni.mpower-social.com";
	
	private static String formsToFilter = "";
	
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(SharedHealthRecordService.class));
	}
	
	JSONParser jsonParser = new JSONParser();
	
	@SuppressWarnings({ "unchecked", "unused", "unused" })
	@Test
	public void encounter() {
		JSONParser jsonParser = new JSONParser();
		Boolean postResponseOfService = true ;
		try {
				String getEncounterUrl = baseOpenmrsUrl +"/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/4ff2410b-7552-464d-9db2-e3d291754c29?includeAll=true";
				String patientencounterResponse = HttpUtil.get(getEncounterUrl, "", "admin:test");
				JSONObject EncounterObj;
				try {
					JSONObject servicesToPost = new JSONObject();
					EncounterObj = (JSONObject) jsonParser.parse(patientencounterResponse);
					JSONArray obs = (JSONArray) EncounterObj.get("observations");
					JSONArray IntialJsonDHISArray =  getObservations(obs);
					Object document = Configuration.defaultConfiguration().jsonProvider().parse(IntialJsonDHISArray.toString());
					List<String> servicesInObservation = JsonPath.read(document, "$..service");
					Set<String> uniqueSetOfServices = new HashSet<>(servicesInObservation);
					System.out.println(uniqueSetOfServices.toString());
					uniqueSetOfServices.forEach(uniqueSetOfService ->{
						List<String> extractServiceJSON = JsonPath.read(document, "$.[?(@.service == '"+uniqueSetOfService+ "')]");
					      String jsonStr = JSONArray.toJSONString(extractServiceJSON);
							try {
								JSONArray extractServiceArray = (JSONArray) jsonParser.parse(jsonStr);
								/*extractServiceArray.forEach(service -> {
									JSONObject serviceObject = (JSONObject) service;
									String elementId = ObserVationDHISMapping.get(serviceObject.get("question"));
									serviceObject.put("elementId", elementId);
								});*/
								JSONObject event = (JSONObject) getEvent().get(uniqueSetOfService);
								
								JSONArray dataValues = new JSONArray();
								for (int i = 0; i < extractServiceArray.size(); i++) {
									JSONObject serviceObject = (JSONObject) extractServiceArray.get(i);
									String field = (String) serviceObject.get("question");
									Object value =  serviceObject.get("answer");
									
									String elementId = ObserVationDHISMapping.get(field);
									JSONObject dataValue = new JSONObject();
									dataValue.put("dataElement", elementId);
									dataValue.put("value", value);
									dataValues.add(dataValue);								
									
								}
								event.put("dataValues", dataValues);
								
								servicesToPost.put(uniqueSetOfService, event);
							} catch (Exception e) {
								e.printStackTrace();
							}
						
					});
					System.out.println(servicesToPost.toString());
					//System.out.println(servicesToPost.size());
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		catch (Exception e) {
			e.printStackTrace();
			postResponseOfService = false;
		}

	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject getEvent(){
		JSONObject serviceEvents = new JSONObject();
		JSONObject clientHistory = new JSONObject();
		clientHistory.put("trackedEntityInstance", "trackedEntityInstance clientHistory");
		clientHistory.put("orgUnit", "orgUnit clientHistory");
		clientHistory.put("program", "program clientHistory");
		clientHistory.put("programStage", "programStage clientHistory");
		clientHistory.put("status", "COMPLETED");
		serviceEvents.put("Client History", clientHistory);
		
		JSONObject inwardReferral = new JSONObject();
		inwardReferral.put("trackedEntityInstance", "inwardReferral");
		inwardReferral.put("orgUnit", "orgUnitinwardReferral");
		inwardReferral.put("program", "programinwardReferral");
		inwardReferral.put("programStage", "programStage inwardReferral");
		inwardReferral.put("status", "COMPLETED");
		serviceEvents.put("Inward Referral", inwardReferral);
		
		
		return serviceEvents;
		
		
	}
	
	@SuppressWarnings("unchecked")
	public static JSONArray getObservations(JSONArray _obs) {
		JSONParser jsonParser = new JSONParser();
		JSONArray observations = new JSONArray();
		
		_obs.forEach(_ob -> {
			JSONObject ob = (JSONObject) _ob;
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
					codedConceptJsonObject.put("question", questionName);
					if(isNumeric(answerValue)) {
						codedConceptJsonObject.put("answer", Double.parseDouble(answerValue));
					}
					else {
						codedConceptJsonObject.put("answer", answerValue);
					}
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
						codedConceptJsonObject.put("question", questionName);
						if(isNumeric(answerValue)) {
							codedConceptJsonObject.put("answer", Double.parseDouble(answerValue));
						}
						else {
							codedConceptJsonObject.put("answer", answerValue);
						}
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
					codedConceptJsonObject.put("question", questionName);
					if(isNumeric(answerValue)) {
						codedConceptJsonObject.put("answer", Double.parseDouble(answerValue));
					}
					else {
						codedConceptJsonObject.put("answer", answerValue);
					}
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
		//System.out.println(observations.toJSONString());
		return observations;
	}
	
//	@SuppressWarnings("unchecked")
//	public static Boolean createVisit (JSONObject obj, String patientUuid) throws Exception {
//		
//		Boolean visitSavingResponse = false;
//		JSONParser jsonParser = new JSONParser();
//		
//		try {
//			
//			String visitUUIdString = (String)obj.get("visitUuid");
//			String visitDetailsByVIsitUuidURL = globalServerUrl + "/openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid=" + visitUUIdString;
//			String visitDetailsByVIsitUuid = HttpUtil.get(visitDetailsByVIsitUuidURL, "", "admin:test");
//			JSONObject visitObject = (JSONObject) jsonParser.parse(visitDetailsByVIsitUuid);
////			JSONObject visitStartJsonObject = new JSONObject();
////			visitStartJsonObject.put("visitType", visitObject.get("visitType"));
////			visitStartJsonObject.put("patient", visitObject.get("patient"));
////			visitStartJsonObject.put("startDatetime", visitObject.get("startDatetime"));
////			visitStartJsonObject.put("stopDatetime", visitObject.get("stopDatetime"));
//			visitObject.put("patient_uuid", patientUuid);
//			visitObject.remove("isFound");
//			String visitSavingUrl = baseOpenmrsUrl + "/openmrs/ws/rest/v1/save-Patient/insert/patientVisitDetails";
//			String visitSavingResponseString = HttpUtil.post(visitSavingUrl, "", visitObject.toString());
//			JSONObject visitSavingObject = (JSONObject) jsonParser.parse(visitSavingResponseString);
//			if(visitSavingObject.containsKey("isSuccessfull")) {
//				Boolean isSuccessfull =  (Boolean)visitSavingObject.get("isSuccessfull");
//				if(isSuccessfull) {
//					visitSavingResponse = true;
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			visitSavingResponse = false;
//		}
//		return visitSavingResponse;
//	}
//	
//	public static final Map<String, String> visitTypeMapping = new HashMap<String, String>();
//	static {
//        visitTypeMapping.put("c228eab1-3f10-11e4-adec-0800271c1b75", "IPD");
//        visitTypeMapping.put("c22a5000-3f10-11e4-adec-0800271c1b75", "OPD");
//        visitTypeMapping.put("bef32e14-3f12-11e4-adec-0800271c1b75", "LAB VISIT");
//	}
	
	public static final Map<String, String> ObserVationDHISMapping = new HashMap<String, String>();
	static {
		ObserVationDHISMapping.put("History of Past Illness", "UXgONLVlRlA");
		ObserVationDHISMapping.put("Family History", "NOOR11jHbxK");
		ObserVationDHISMapping.put("Chief Complaint", "d49IcANqJh9");
	}
	
	public static boolean isNumeric(String str) {
		return str.matches("[0-9.]*");
	}
	
}
