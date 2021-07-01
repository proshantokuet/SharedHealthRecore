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


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;


import ca.uhn.hl7v2.model.primitive.ID;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;


/**
 * Tests {@link $ SharedHealthRecordService} .
 */
public class ObservationServiceTest extends BaseModuleContextSensitiveTest {
	
	//private final static String baseOpenmrsUrl = "https://192.168.19.145";
	
	private final static String globalServerUrl = "https://192.168.19.47";
	
	private final static String trackInstanceUrl = "http://192.168.19.149" + "/api/trackedEntityInstances.json?";
	
	private static String orgUnitString = "";
	
	private static String trackeEntityInstanceIDString = "";
	
	Gson gson = new Gson();
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(SharedHealthRecordService.class));
	}
	
	JSONParser jsonParser = new JSONParser();
	
	@SuppressWarnings({ "unchecked", "unused" })

	public void encounter() {
		JSONParser jsonParser = new JSONParser();
		Boolean postResponseOfService = true ;
		try {   
			String a = "/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/7f9734f4-310a-4804-90c1-da9de1c6a4e9?includeAll=true";
//			String encounterUUid = a.split("/|\\?")[7];
//			String patientVisitUrl = globalServerUrl + "/openmrs/ws/rest/v1/visit?includeInactive=true&patient=9802ff9f-b317-4e39-aa9a-6475b41fafe7&v=custom:(encounters:(uuid))";
//		    String patientVisitResponse = HttpUtil.get(patientVisitUrl, "", "admin:test");
		   // JSONObject patientVisitObject = (JSONObject) jsonParser.parse(patientVisitResponse);
				String getEncounterUrl = globalServerUrl +"/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/581753b1-065b-44da-b7c6-3f656f997044?includeAll=true";
				String patientencounterResponse = HttpUtil.get(getEncounterUrl, "", "superman:Admin123");
				JSONObject EncounterObj;
				
				try {
					JSONObject servicesToPost = new JSONObject();
					EncounterObj = (JSONObject) jsonParser.parse(patientencounterResponse);
					System.out.println(EncounterObj.toString());
					String patientUuid = (String)EncounterObj.get("patientUuid");
					String encounterUUid = a.split("/|\\?")[7];
//					JSONObject patientEventInformation = getDhisEventInformation(patientUuid);
//					org.json.JSONObject testt = new org.json.JSONObject(patientEventInformation);
//					boolean  patientEventStatus = testt.getBoolean("patientEventStatus");
//					String orgUnit = testt.getString("orgUnit");
//					String tackedEntityInstance = testt.getString("tackedEntityInstance");
//					System.out.println(patientEventStatus);
					JSONArray obs = (JSONArray) EncounterObj.get("observations");


					JSONArray IntialJsonDHISArray =  getUBSObservations(obs);
					
//					List<dhisObsDataMap> data = new ArrayList<dhisObsDataMap>();
//					IntialJsonDHISArray.forEach(initialjson -> {
//						String  jsonString = initialjson.toString();
//				        Gson gson = new Gson();  
//				        data.add(gson.fromJson(jsonString,dhisObsDataMap.class)); 
//					});
//					List<String> servicesInObservationForms = new ArrayList<String>();
//			        for (dhisObsDataMap serviceName : data) {
//			        	servicesInObservationForms.add(serviceName.getService());
//						System.out.println(serviceName.getService());
//					}
//					Set<String> uniqueSetOfServicesForms = new HashSet<>(servicesInObservationForms);
//			        System.out.println(uniqueSetOfServicesForms.toString());
//			        
//			        List<dhisObsDataMap> beerDrinkers = data.stream()
//			        	    .filter(p -> p.getService().equals("Client History") && p.getVoidReason() == null).collect(Collectors.toList());
//			        
//			        for (dhisObsDataMap dhisObsDataMap : beerDrinkers) {
//						System.out.println(dhisObsDataMap.getQuestion());
//						System.out.println(dhisObsDataMap.getAnswer());
//						System.out.println(dhisObsDataMap.getService());
//						System.out.println(dhisObsDataMap.getVoidReason());
//					}
			        
					String teString = IntialJsonDHISArray.toString();
					System.out.println(teString);
					//Object document = Configuration.defaultConfiguration().jsonProvider().parse(IntialJsonDHISArray.toString());
					//String formsName = "Client History";
					List<String> servicesInObservation = JsonPath.read(teString, "$..service");
					Set<String> uniqueSetOfServices = new HashSet<>();
					uniqueSetOfServices.addAll(servicesInObservation);
					System.out.println(uniqueSetOfServices.toString());
					uniqueSetOfServices.forEach(uniqueSetOfService ->{
						List<String> extractServiceJSON = JsonPath.read(teString, "$.[?(@.service == '"+uniqueSetOfService+ "' && @.isVoided == false)]");
						List<Object> testObjects = new ArrayList<Object>(extractServiceJSON);
					      String jsonStr = JSONArray.toJSONString(extractServiceJSON);
							try {
								JSONArray extractServiceArray = (JSONArray) jsonParser.parse(jsonStr);
								String tableName = UBSTABLE_MAP.get(uniqueSetOfService);
								if(StringUtils.isBlank(tableName)) {
									
								}
								/*extractServiceArray.forEach(service -> {
									JSONObject serviceObject = (JSONObject) service;
									String elementId = ObserVationDHISMapping.get(serviceObject.get("question"));
									serviceObject.put("elementId", elementId);
								});*/
								//JSONObject event = (JSONObject) getEvent(tackedEntityInstance,orgUnit, uniqueSetOfService).get(uniqueSetOfService);
								//if(event !=null) {
								
/*								JSONArray dataValues = new JSONArray();
								for (int i = 0; i < extractServiceArray.size(); i++) {
									JSONObject serviceObject = (JSONObject) extractServiceArray.get(i);
									String field = (String) serviceObject.get("question");
									Object value =  serviceObject.get("answer");
									
									String elementId = ObserVationDHISMapping.get(field);
									if (!StringUtils.isEmpty(elementId)){
									JSONObject dataValue = new JSONObject();
									dataValue.put("dataElement", elementId);
									dataValue.put("value", value);
									dataValues.add(dataValue);			
									}
									else {
										String elementIdMultiple = multipleObsDHISMapping.get(value);
										if (!StringUtils.isEmpty(elementIdMultiple)){
											JSONObject dataValue = new JSONObject();
											dataValue.put("dataElement", elementIdMultiple);
											dataValue.put("value", "Yes");
											dataValues.add(dataValue);			
											}
									}
								}
								System.out.println(dataValues);
								event.put("dataValues", dataValues);
								System.out.println(event);
								servicesToPost.put(uniqueSetOfService, event);*/
								//}
//								org.json.JSONArray datavaluespractice = new org.json.JSONArray();
//								String json = new Gson().toJson(extractServiceJSON);
//								org.json.JSONArray practceJsonArray = new org.json.JSONArray(json);
//								System.out.println(practceJsonArray.toString());
//								for (int i = 0; i < practceJsonArray.length(); i++) {
//									org.json.JSONObject serviceObject = practceJsonArray.getJSONObject(i);
////									String field = serviceObject.getString("question");
////									String value = serviceObject.getString("answer");
////									String elementId = ObserVationDHISMapping.get(field);
////									if (!StringUtils.isEmpty(elementId)){
////										org.json.JSONObject dataValue = new org.json.JSONObject();
////										dataValue.put("dataElement", elementId);
////										if(isNumeric(value)) {
////											dataValue.put("value", Double.parseDouble(value));
////										}
////										else {
////											dataValue.put("value", value);
////										}
////										datavaluespractice.put(dataValue);			
////									}
//								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						
					});
					
/*					
					org.json.JSONObject test = new org.json.JSONObject(servicesToPost);
					org.json.JSONArray keys = test.names();
					if (keys !=null) {
					for (int i = 0; i < keys.length(); i++) {
						
						String formsNadme = keys.getString(i); // Here's your key
						String value = test.getString(formsNadme);
						org.json.JSONObject test1 = new org.json.JSONObject(value);
						 System.out.println(test1);
					}
					//System.out.println(servicesToPost.toString());
					servicesToPost.keySet().forEach(keyStr ->
				    {
				        JSONObject object = (JSONObject) servicesToPost.get(keyStr);
				        System.out.println("key: "+ keyStr + " value: " + object);

				    });
					System.out.println(servicesToPost.size());
				  }*/
				}
				catch (Exception e) {
					e.printStackTrace();
				}
		}
		catch (Exception e) {
			e.printStackTrace();
			postResponseOfService = false;
		}

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void extractAndSave() {
		JSONParser jsonParser = new JSONParser();
		try {   
				String url = "/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/be6c9cf4-3fc1-4f93-b27a-af71bda05bba?includeAll=true";
				String getEncounterUrl = globalServerUrl + url;
				
				String patientencounterResponse = HttpUtil.get(getEncounterUrl, "", "superman:Admin123");
				JSONObject EncounterObj;

				EncounterObj = (JSONObject) jsonParser.parse(patientencounterResponse);
				System.out.println(EncounterObj.toString());
				String patientUuid = (String)EncounterObj.get("patientUuid");
				String encounterUUid = url.split("/|\\?")[7];

				JSONArray obs = (JSONArray) EncounterObj.get("observations");

				JSONArray IntialJsonDHISArray =  getUBSObservations(obs);
				
		        if(IntialJsonDHISArray.size() > 0) {
					String teString = IntialJsonDHISArray.toString();
					System.out.println(teString);
	
					List<String> servicesInObservation = JsonPath.read(teString, "$..service");
					Set<String> uniqueSetOfServices = new HashSet<>();
					uniqueSetOfServices.addAll(servicesInObservation);
					System.out.println(uniqueSetOfServices.toString());
					uniqueSetOfServices.forEach(uniqueSetOfService ->{
						List<String> extractServiceJSON = JsonPath.read(teString, "$.[?(@.service == '"+uniqueSetOfService+ "' && @.isVoided == false)]");
					    String jsonStr = JSONArray.toJSONString(extractServiceJSON);
						//String jsonStr = "shanto";
							try {
								JSONArray extractServiceArray = (JSONArray) jsonParser.parse(jsonStr);
								String tableName = UBSTABLE_MAP.get(uniqueSetOfService);
								if(!StringUtils.isBlank(tableName)) {
									extractServiceArray.forEach(service -> {
										JSONObject serviceObject = (JSONObject) service;
										serviceObject.put("patientUuid", patientUuid);
										serviceObject.put("encounterUuid", encounterUUid);
									});
	
									System.out.println(extractServiceArray.toString());
									 List<dhisObsDataMap> data = gson.fromJson(extractServiceArray.toString(),
										    new TypeToken<ArrayList<dhisObsDataMap>>() {}.getType());
									System.out.println(data.size());
									for (dhisObsDataMap obsData : data) {
										//obsData = null;
										if(obsData.getAnswer().equalsIgnoreCase("No")) {
											throw new RuntimeException();
										}
										else {
										System.out.println(obsData.getQuestion());
										System.out.println(obsData.getAnswer());
										System.out.println(obsData.getPatientUuid());
										System.out.println(obsData.getEncounterUuid());
										}
									}
								}
							} catch (ParseException e) {
								e.printStackTrace();
								throw new RuntimeException();
							}
						
					});
					System.out.println("Success fully Completed");
		        }
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject getEvent(String trackeEntityInstance, String orgUnit, String FormName){
		JSONObject serviceEvents = new JSONObject();
		try {
			Date date = Calendar.getInstance().getTime();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String today = dateFormat.format(date);
			
			switch (FormName) {
			case "Client History":
				JSONObject clientHistory = new JSONObject();
				clientHistory.put("trackedEntityInstance", trackeEntityInstance);
				clientHistory.put("orgUnit", orgUnit);
				clientHistory.put("program", "q2uZRqRc0UD");
				clientHistory.put("programStage", "gMhLM145zph");
				clientHistory.put("status", "COMPLETED");
				clientHistory.put("eventDate", today);
				serviceEvents.put("Client History", clientHistory);
				break;
			case "General Examination":
				JSONObject generalExamination = new JSONObject();
				generalExamination.put("trackedEntityInstance", trackeEntityInstance);
				generalExamination.put("orgUnit", orgUnit);
				generalExamination.put("program", "q2uZRqRc0UD");
				generalExamination.put("programStage", "MsQuyOp8mRh");
				generalExamination.put("status", "COMPLETED");
				generalExamination.put("eventDate", today);
				serviceEvents.put("General Examination", generalExamination);
				break;
			case "Delivery":
				JSONObject delivery = new JSONObject();
				delivery.put("trackedEntityInstance", trackeEntityInstance);
				delivery.put("orgUnit", orgUnit);
				delivery.put("program", "q2uZRqRc0UD");
				delivery.put("programStage", "IuoFEwM0dP4");
				delivery.put("status", "COMPLETED");
				delivery.put("eventDate", today);
				serviceEvents.put("Delivery", delivery);
				break;
			case "Limited Curative Care":
				JSONObject lcc = new JSONObject();
				lcc.put("trackedEntityInstance", trackeEntityInstance);
				lcc.put("orgUnit", orgUnit);
				lcc.put("program", "q2uZRqRc0UD");
				lcc.put("programStage", "sETH3JvTMR4");
				lcc.put("status", "COMPLETED");
				lcc.put("eventDate", today);
				serviceEvents.put("Limited Curative Care", lcc);
				break;
			case "Women Vaccination 15 to 49 Years old":
				JSONObject womenVaccination = new JSONObject();
				womenVaccination.put("trackedEntityInstance", trackeEntityInstance);
				womenVaccination.put("orgUnit", orgUnit);
				womenVaccination.put("program", "q2uZRqRc0UD");
				womenVaccination.put("programStage", "OAVc5c3vIiV");
				womenVaccination.put("status", "COMPLETED");
				womenVaccination.put("eventDate", today);
				serviceEvents.put("Women Vaccination 15 to 49 Years old", womenVaccination);
				break;
			case "STI and RTI":
				JSONObject stiAndRti = new JSONObject();
				stiAndRti.put("trackedEntityInstance", trackeEntityInstance);
				stiAndRti.put("orgUnit", orgUnit);
				stiAndRti.put("program", "q2uZRqRc0UD");
				stiAndRti.put("programStage", "bwq2DoZANeK");
				stiAndRti.put("status", "COMPLETED");
				stiAndRti.put("eventDate", today);
				serviceEvents.put("STI and RTI", stiAndRti);
				break;
			case "Cervical Cancer":
				JSONObject cervicalCancer = new JSONObject();
				cervicalCancer.put("trackedEntityInstance", trackeEntityInstance);
				cervicalCancer.put("orgUnit", orgUnit);
				cervicalCancer.put("program", "q2uZRqRc0UD");
				cervicalCancer.put("programStage", "AhNyAtFbZ32");
				cervicalCancer.put("status", "COMPLETED");
				cervicalCancer.put("eventDate", today);
				serviceEvents.put("Cervical Cancer", cervicalCancer);
				break;
			case "Family Planning":
				JSONObject familyPlanning = new JSONObject();
				familyPlanning.put("trackedEntityInstance", trackeEntityInstance);
				familyPlanning.put("orgUnit", orgUnit);
				familyPlanning.put("program", "q2uZRqRc0UD");
				familyPlanning.put("programStage", "vLyly6fRKFX");
				familyPlanning.put("status", "COMPLETED");
				familyPlanning.put("eventDate", today);
				serviceEvents.put("Family Planning", familyPlanning);
				break;
			case "Post Abortion Care":
				JSONObject pacs = new JSONObject();
				pacs.put("trackedEntityInstance", trackeEntityInstance);
				pacs.put("orgUnit", orgUnit);
				pacs.put("program", "q2uZRqRc0UD");
				pacs.put("programStage", "SCOvq6tbHep");
				pacs.put("status", "COMPLETED");
				pacs.put("eventDate", today);
				serviceEvents.put("Post Abortion Care", pacs);
				break;
			case "Eye Care":
				JSONObject eyeCare = new JSONObject();
				eyeCare.put("trackedEntityInstance", trackeEntityInstance);
				eyeCare.put("orgUnit", orgUnit);
				eyeCare.put("program", "q2uZRqRc0UD");
				eyeCare.put("programStage", "e3rWjcoeYYt");
				eyeCare.put("status", "COMPLETED");
				eyeCare.put("eventDate", today);
				serviceEvents.put("Eye Care", eyeCare);
				break;
			case "Discharge Certificate":
				JSONObject discharge = new JSONObject();
				discharge.put("trackedEntityInstance", trackeEntityInstance);
				discharge.put("orgUnit", orgUnit);
				discharge.put("program", "q2uZRqRc0UD");
				discharge.put("programStage", "WmTXHPPpxU3");
				discharge.put("status", "COMPLETED");
				discharge.put("eventDate", today);
				serviceEvents.put("Discharge Certificate", discharge);
				break;
			case "Antenatal Care":
				JSONObject antenatalCare = new JSONObject();
				antenatalCare.put("trackedEntityInstance", trackeEntityInstance);
				antenatalCare.put("orgUnit", orgUnit);
				antenatalCare.put("program", "q2uZRqRc0UD");
				antenatalCare.put("programStage", "yljgHb76CZm");
				antenatalCare.put("status", "COMPLETED");
				antenatalCare.put("eventDate", today);
				serviceEvents.put("Antenatal Care", antenatalCare);
				break;
			case "Postnatal Care":
				JSONObject pnc = new JSONObject();
				pnc.put("trackedEntityInstance", trackeEntityInstance);
				pnc.put("orgUnit", orgUnit);
				pnc.put("program", "q2uZRqRc0UD");
				pnc.put("programStage", "rEqTh49ChIx");
				pnc.put("status", "COMPLETED");
				pnc.put("eventDate", today);
				serviceEvents.put("Postnatal Care", pnc);
				break;
			case "Vaccination for Child":
				JSONObject vaccinationChild = new JSONObject();
				vaccinationChild.put("trackedEntityInstance", trackeEntityInstance);
				vaccinationChild.put("orgUnit", orgUnit);
				vaccinationChild.put("program", "q2uZRqRc0UD");
				vaccinationChild.put("programStage", "GNQSZ6ZMnys");
				vaccinationChild.put("status", "COMPLETED");
				vaccinationChild.put("eventDate", today);
				serviceEvents.put("Vaccination for Child", vaccinationChild);
				break;
			case "Adolescent Health":
				JSONObject adolescent = new JSONObject();
				adolescent.put("trackedEntityInstance", trackeEntityInstance);
				adolescent.put("orgUnit", orgUnit);
				adolescent.put("program", "q2uZRqRc0UD");
				adolescent.put("programStage", "DDrycKd33cw");
				adolescent.put("status", "COMPLETED");
				adolescent.put("eventDate", today);
				serviceEvents.put("Adolescent Health", adolescent);
				break;
			case "First Aid":
				JSONObject firstAid = new JSONObject();
				firstAid.put("trackedEntityInstance", trackeEntityInstance);
				firstAid.put("orgUnit", orgUnit);
				firstAid.put("program", "q2uZRqRc0UD");
				firstAid.put("programStage", "i8djL6jzrxP");
				firstAid.put("status", "COMPLETED");
				firstAid.put("eventDate", today);
				serviceEvents.put("First Aid", firstAid);
				break;
			case "IMCI (age below 2 months)":
				JSONObject imciChild = new JSONObject();
				imciChild.put("trackedEntityInstance", trackeEntityInstance);
				imciChild.put("orgUnit", orgUnit);
				imciChild.put("program", "q2uZRqRc0UD");
				imciChild.put("programStage", "OZ61jJpZOEn");
				imciChild.put("status", "COMPLETED");
				imciChild.put("eventDate", today);
				serviceEvents.put("IMCI (age below 2 months)", imciChild);
				break;
			case "IMCI (age 2 months to 5 years)":
				JSONObject imciAdult = new JSONObject();
				imciAdult.put("trackedEntityInstance", trackeEntityInstance);
				imciAdult.put("orgUnit", orgUnit);
				imciAdult.put("program", "q2uZRqRc0UD");
				imciAdult.put("programStage", "wMSY39GW3sl");
				imciAdult.put("status", "COMPLETED");
				imciAdult.put("eventDate", today);
				serviceEvents.put("IMCI (age 2 months to 5 years)", imciAdult);
				break;
			case "Obstetric History":
				JSONObject obstetric = new JSONObject();
				obstetric.put("trackedEntityInstance", trackeEntityInstance);
				obstetric.put("orgUnit", orgUnit);
				obstetric.put("program", "q2uZRqRc0UD");
				obstetric.put("programStage", "rJGtR1OgqNV");
				obstetric.put("status", "COMPLETED");
				obstetric.put("eventDate", today);
				serviceEvents.put("Obstetric History", obstetric);
				break;
			case "New Born Baby Care":
				JSONObject newBorn = new JSONObject();
				newBorn.put("trackedEntityInstance", trackeEntityInstance);
				newBorn.put("orgUnit", orgUnit);
				newBorn.put("program", "q2uZRqRc0UD");
				newBorn.put("programStage", "m0rBW5OOwQO");
				newBorn.put("status", "COMPLETED");
				newBorn.put("eventDate", today);
				serviceEvents.put("New Born Baby Care", newBorn);
				break;
			case "General Vaccination":
				JSONObject generalVaccination = new JSONObject();
				generalVaccination.put("trackedEntityInstance", trackeEntityInstance);
				generalVaccination.put("orgUnit", orgUnit);
				generalVaccination.put("program", "q2uZRqRc0UD");
				generalVaccination.put("programStage", "d2dVaBF6cqY");
				generalVaccination.put("status", "COMPLETED");
				generalVaccination.put("eventDate", today);
				serviceEvents.put("General Vaccination", generalVaccination);
				break;
			default:
				break;
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		    return serviceEvents;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONArray getObservations(JSONArray _obs) {
		JSONParser jsonParser = new JSONParser();
		JSONArray observations = new JSONArray();
		
		_obs.forEach(_ob -> {
			JSONObject ob = (JSONObject) _ob;
			System.out.println(ob.toString());
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
					String voidreason = (String) ob.get("voidReason");
					boolean isVoided = (boolean) ob.get("voided");
					codedConceptJsonObject.put("question", questionName);
					codedConceptJsonObject.put("voidReason", voidreason);
					codedConceptJsonObject.put("isVoided", isVoided);
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
						String voidreason = (String) ob.get("voidReason");
						boolean isVoided = (boolean) ob.get("voided");
						codedConceptJsonObject.put("question", questionName);
						codedConceptJsonObject.put("voidReason", voidreason);
						codedConceptJsonObject.put("isVoided", isVoided);
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
					String voidreason = (String) ob.get("voidReason");
					boolean isVoided = (boolean) ob.get("voided");
					codedConceptJsonObject.put("question", questionName);
					codedConceptJsonObject.put("voidReason", voidreason);
					codedConceptJsonObject.put("isVoided", isVoided);
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
		System.out.println(observations.toJSONString());
		return observations;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONArray getUBSObservations(JSONArray _obs) {
		JSONParser jsonParser = new JSONParser();
		JSONArray observations = new JSONArray();
		
		_obs.forEach(_ob -> {
			JSONObject ob = (JSONObject) _ob;
			System.out.println(ob.toString());
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
						//String voidreason = (String) ob.get("voidReason");
						boolean isVoided = (boolean) ob.get("voided");
						codedConceptJsonObject.put("question", questionName);
						//codedConceptJsonObject.put("voidReason", voidreason);
						codedConceptJsonObject.put("isVoided", isVoided);
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
					//String voidreason = (String) ob.get("voidReason");
					boolean isVoided = (boolean) ob.get("voided");
					codedConceptJsonObject.put("question", questionName);
					//codedConceptJsonObject.put("voidReason", voidreason);
					codedConceptJsonObject.put("isVoided", isVoided);
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
		System.out.println(observations.toJSONString());
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

		ObserVationDHISMapping.put("Chief Complaint", "PV0CvNB9vV3");
		ObserVationDHISMapping.put("Duration (Days)", "p8s08fLaaDF");
		ObserVationDHISMapping.put("History of Past Illness", "POAuU2vktvD");
		ObserVationDHISMapping.put("Other Notes", "EpfZFHi0OMz");
		ObserVationDHISMapping.put("Family History", "bZWuZ4IlqDU");
		ObserVationDHISMapping.put("Personal History", "LqKfg3PqKDo");
		ObserVationDHISMapping.put("Self Blood Group", "PWyWxiWu6Kj");
		ObserVationDHISMapping.put("Spouse Blood Group", "l7ow9Y8Bv4X");
		ObserVationDHISMapping.put("Drug History", "KlLvwN3Pvcm");
		ObserVationDHISMapping.put("Vaccination Date", "iwAEjX2zYzs");
		ObserVationDHISMapping.put("Next Vaccination Date", "ZnD4ie9ybHV");
		ObserVationDHISMapping.clear();
	}
	
	public static final Map<String, String> multipleObsDHISMapping = new HashMap<String, String>();
	static {

		multipleObsDHISMapping.put("Follow-up For", "lDoRWF6RqUz");
		multipleObsDHISMapping.put("PCV-2", "q9pysMjVr0t");
		multipleObsDHISMapping.put("Penta-1", "Eqi7X93lEev");
		multipleObsDHISMapping.put("OPV-0", "tS4KMt16ExA");
		multipleObsDHISMapping.put("OPV-1", "QsLMJHguGQP");
		multipleObsDHISMapping.put("OPV-2", "h3bFMtOVj6U");
		multipleObsDHISMapping.put("OPV-3", "LKA6fPEju09");
		multipleObsDHISMapping.put("Vitamin A", "uJa7m5oBiE6");
		multipleObsDHISMapping.put("De-worming", "a6DDwehoRGt");
		ObserVationDHISMapping.clear();
	}
	
	
	public static final Map<String, String> UBSTABLE_MAP = new HashMap<String, String>();
	static {

		UBSTABLE_MAP.put("Acute Health Condition", "acute_health_condition");
		UBSTABLE_MAP.put("Sexual & Reproductive health (SRH)", "sexual_reproductive_health");
	}
	
	public static boolean isNumeric(String str) {
		return str.matches("[0-9.]*");
	}
	
	@SuppressWarnings("unchecked")
	public  JSONObject getDhisEventInformation(String patientUuid) throws JSONException {
		JSONParser jsonParser = new JSONParser();
		boolean patientEventStatus = true;
		JSONObject eventInformationObject = new JSONObject();
		String orgUnit = "";
		String tackedEntityInstance = "";
		try {
			String patientUrl = globalServerUrl + "/openmrs/ws/rest/v1/patient/"+patientUuid+"?v=full";
			String patientResponse = HttpUtil.get(patientUrl, "", "admin:test");
			JSONObject patient = (JSONObject) jsonParser.parse(patientResponse);
			System.out.println(patient.toJSONString());
			JSONObject person = (JSONObject) patient.get("person");
			JSONArray patientAttributes = (JSONArray) person.get("attributes");
			patientAttributes.forEach(_patientAttribute -> {
				JSONObject patientAttribute = (JSONObject) _patientAttribute;
				JSONObject attributeType = (JSONObject) patientAttribute.get("attributeType");
				String attributeTypeName = (String) attributeType.get("display");
				if ("orgUnit".equalsIgnoreCase(attributeTypeName)) {
					orgUnitString = (String) patientAttribute.get("value");
				}
			});
		} 

		catch (Exception e) {
			// TODO: handle exception
		}
		
		String URL = trackInstanceUrl + "filter=" + "oW51s5NUIqo" + ":EQ:" + patientUuid + "&ou=" + orgUnitString;
		String trackentityIsntances = HttpUtil.get(URL, "", "apiadmin:Apiadmin@123");
		JSONObject getResponse;
		try {
			getResponse = (JSONObject) jsonParser.parse(trackentityIsntances);
			JSONArray trackedEntityInstances = new JSONArray();
			if (getResponse.containsKey("trackedEntityInstances")) {
				trackedEntityInstances = (JSONArray) getResponse.get("trackedEntityInstances");
			}

			if (trackedEntityInstances.size() != 0) {
				JSONObject trackedEntityInstance = (JSONObject) trackedEntityInstances.get(0);
				trackeEntityInstanceIDString = (String) trackedEntityInstance.get("trackedEntityInstance");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (StringUtils.isEmpty(orgUnitString) || StringUtils.isEmpty(trackeEntityInstanceIDString)) {
			patientEventStatus = false;
			eventInformationObject.put("orgUnit", orgUnitString);
			eventInformationObject.put("tackedEntityInstance", trackeEntityInstanceIDString);
			eventInformationObject.put("patientEventStatus", patientEventStatus);
		}
		else {
			patientEventStatus = true;
			eventInformationObject.put("orgUnit", orgUnitString);
			eventInformationObject.put("tackedEntityInstance", trackeEntityInstanceIDString);
			eventInformationObject.put("patientEventStatus", patientEventStatus);
		}
		return eventInformationObject;
	}
	
}
