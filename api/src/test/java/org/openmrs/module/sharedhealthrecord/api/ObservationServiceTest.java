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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.openmrs.api.context.Context;
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void encounter() {
		
		JSONParser jsonParser = new JSONParser();
		
		try (FileReader reader = new FileReader("../obs.json")) {
			//try {
			//String getEncounterUrl = "https://192.168.19.145//openmrs/ws/rest/v1/bahmnicore/bahmniencounter/2c1921f4-f54c-4ad4-b852-94d7563fb67d?includeAll=true";
			
			//String patientResponse = HttpUtil.get(getEncounterUrl, "", "sohel:Sohel@123");
			//Read JSON file
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
			JSONArray obs = (JSONArray) obj.get("observations");
			JSONArray pro = (JSONArray) obj.get("providers");
			JSONObject encounter = new JSONObject();
			JSONArray obervations = getObservations(obs);
			//System.out.println(obj);
			encounter.put("locationUuid", obj.get("locationUuid"));
			encounter.put("patientUuid", obj.get("patientUuid"));
			encounter.put("visitUuid", obj.get("visitUuid"));
			encounter.put("encounterDateTime", obj.get("encounterDateTime"));
			encounter.put("visitType", obj.get("visitType"));
			encounter.put("patientUuid", obj.get("patientUuid"));
			encounter.put("observations", obervations);
			encounter.put("extensions", new JSONObject());
			encounter.put("context", new JSONObject());
			encounter.put("bahmniDiagnoses", new JSONArray());
			encounter.put("orders", new JSONArray());
			encounter.put("drugOrders", new JSONArray());
			encounter.put("drugOrders", new JSONArray());
			encounter.put("encounterUuid", obj.get("encounterUuid"));
			System.err.println();
			JSONArray providers = new JSONArray();
			JSONObject provider = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(pro.get(0).toString(),
			    Provider.class)));
			providers.add(provider);
			System.err.println(provider);
			encounter.put("providers", providers);
			System.err.println(encounter);
			
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
			JSONObject observation = new JSONObject();
			JSONObject concept = new JSONObject();
			JSONObject _concept = (JSONObject) ob.get("concept");
			concept.put("uuid", _concept.get("conceptUuid"));
			concept.put("name", _concept.get("conceptNameToDisplay"));
			observation.put("concept", concept);
			observation.put("value", _concept.get("valueAsString"));
			observation.put("formNamespace", ob.get("formNamespace"));
			observation.put("formFieldPath", ob.get("formFieldPath"));
			observation.put("formFieldPath", ob.get("formFieldPath"));
			String type = (String) ob.get("type");
			try {
				if (type.equalsIgnoreCase("Coded")) {
					JSONObject obs = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(ob.toString(),
					    ObservationWithValues.class)));
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
				//e.printStackTrace();
			}
		});
		return observations;
	}
	
}
