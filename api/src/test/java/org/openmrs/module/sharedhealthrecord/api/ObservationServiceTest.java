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

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.domain.Encounter;
import org.openmrs.module.sharedhealthrecord.domain.Observation;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithGroupMemebrs;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithValues;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.web.servlet.result.PrintingResultHandler;

import ca.uhn.hl7v2.model.v25.segment.IIM;

import com.google.gson.Gson;

/**
 * Tests {@link $ SharedHealthRecordService} .
 */
public class ObservationServiceTest extends BaseModuleContextSensitiveTest {
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(SharedHealthRecordService.class));
	}
	
	JSONParser jsonParser = new JSONParser();
	
	@SuppressWarnings("unchecked")
	@Test
	public void encounter() {
		
		JSONParser jsonParser = new JSONParser();
		
			try {
	        
			String patientVisitUrl = "https://192.168.19.145/openmrs/ws/rest/v1/visit?includeInactive=true&patient=0074de49-bd7e-427a-a192-e94195533cd6&v=custom:(encounters:(uuid))";
			String patientVisitResponse = HttpUtil.get(patientVisitUrl, "", "admin:test");
			JSONObject patientVisitObject = (JSONObject) jsonParser.parse(patientVisitResponse);
			JSONArray visitsArray = (JSONArray) patientVisitObject.get("results");
		    visitsArray.forEach(_ob -> {
				JSONObject encountersObject = (JSONObject) _ob;
				JSONArray encountersArray = (JSONArray) encountersObject.get("encounters");
				encountersArray.forEach(_enc -> {
					JSONObject singleEncountersObject = (JSONObject) _enc;
					String encounterUuid = (String) singleEncountersObject.get("uuid");
					String getEncounterUrl = "https://192.168.19.145/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"+encounterUuid+"?includeAll=true";
					String patientencounterResponse = HttpUtil.get(getEncounterUrl, "", "admin:test");
					//System.out.println("patientencounterResponse" + patientencounterResponse);

					// System.out.println("json parse starting...........");
					JSONObject obj;

					try {
						obj = (JSONObject) jsonParser.parse(patientencounterResponse);
						String visitUUIdString = (String)obj.get("visitUuid");
						String visitDetailsByVIsitUuidURL = "https://192.168.19.145/openmrs/ws/rest/v1/visit/"+visitUUIdString+"?includeAll=true";
						String visitDetailsByVIsitUuid = HttpUtil.get(visitDetailsByVIsitUuidURL, "", "admin:test");
						JSONObject visitObject = (JSONObject) jsonParser.parse(visitDetailsByVIsitUuid);
						JSONObject visitStartJsonObject = new JSONObject();
						visitStartJsonObject.put("visitType", visitObject.get("visitType"));
						visitStartJsonObject.put("patient", visitObject.get("patient"));
						visitStartJsonObject.put("startDatetime", visitObject.get("startDatetime"));
						visitStartJsonObject.put("stopDatetime", visitObject.get("stopDatetime"));
						String visitSavingUrl = "https://192.168.19.147/openmrs/ws/rest/v1/visit";
						String visitSavingResponse = HttpUtil.post(visitSavingUrl, "", visitStartJsonObject.toString());
						 //String visitSavingResponse = HttpUtil.get(visitSavingUrl, "", "admin:test");
						JSONObject visitJsonAfterSaving = (JSONObject) jsonParser.parse(visitSavingResponse);
						System.out.println("visitJsonAfterSaving" + visitJsonAfterSaving);
						String visitUuid = (String) visitJsonAfterSaving.get("uuid");
						String visitTypeUuidString = (String)obj.get("visitTypeUuid");
						String visitTypeValue = visitTypeMapping.get(visitTypeUuidString);
						obj.put("visitType", visitTypeValue);
						obj.remove("visitUuid");
						obj.put("visitUuid", visitUuid);
						JSONArray obs = (JSONArray) obj.get("observations");
						JSONArray obervations = getObservations(obs);
						JSONObject encounter = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(obj.toString(),Encounter.class)));
						encounter.put("observations", obervations);
						System.out.println("parsed Json :" + encounter.toJSONString());
						String patientServiceUrl = "https://192.168.19.147/openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
						String postResponse = HttpUtil.post(patientServiceUrl, "", encounter.toJSONString());
						System.out.println("After Post Request Response :" + postResponse.toString());
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
		    });
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
					
					JSONObject obs = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(ob.toString(),
					    ObservationWithValues.class)));
					observations.add(obs);
				} else if (groupMembers.size() != 0) {
					
					JSONObject obs = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(ob.toString(),
					    ObservationWithGroupMemebrs.class)));
					observations.add(obs);
				} else {
					
					JSONObject obs = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(ob.toString(),
					    Observation.class)));
					observations.add(obs);
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
	
	public static final Map<String, String> visitTypeMapping = new HashMap<String, String>();
	static {
        visitTypeMapping.put("c228eab1-3f10-11e4-adec-0800271c1b75", "IPD");
        visitTypeMapping.put("c22a5000-3f10-11e4-adec-0800271c1b75", "OPD");
        visitTypeMapping.put("bef32e14-3f12-11e4-adec-0800271c1b75", "LAB VISIT");
	}
	
}
