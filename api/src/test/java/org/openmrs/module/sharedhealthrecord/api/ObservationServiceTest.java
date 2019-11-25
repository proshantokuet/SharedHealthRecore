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
import org.openmrs.test.BaseModuleContextSensitiveTest;

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
		
		try (FileReader reader = new FileReader("../drug_order.json")) {
			/*try {
				String getEncounterUrl = "https://192.168.19.145/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/2c1921f4-f54c-4ad4-b852-94d7563fb67d?includeAll=true";
				
				String patientResponse = HttpUtil.get(getEncounterUrl, "", "sohel:Sohel@123");*/
			//Read JSON file
			System.out.println("json parse starting...........");
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
			JSONArray obs = (JSONArray) obj.get("observations");
			
			JSONArray obervations = getObservations(obs);
			
			JSONObject encounter = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(obj.toString(),
			    Encounter.class)));
			encounter.put("observations", obervations);
			System.out.println(encounter);
			
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
			System.out.println("Coded..........:" + groupMembers.size() + " type:" + type);
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
	
}
