package org.openmrs.module.sharedhealthrecord.web.controller.rest;

import java.util.UUID;
import org.openmrs.api.context.Context;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;
import org.openmrs.module.sharedhealthrecord.domain.Encounter;
import org.openmrs.module.sharedhealthrecord.domain.Observation;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithGroupMemebrs;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithValues;
import org.openmrs.module.sharedhealthrecord.domain.PersonAddress;
import org.openmrs.module.sharedhealthrecord.domain.PreferredName;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@RequestMapping("/rest/v1/save-Patient")
@RestController
public class SharedHealthRecordManageRestController {

	@RequestMapping(value = "/patient/toLocalServer", method = RequestMethod.GET)
	public ResponseEntity<String> getPatientFromGlobalServer(@RequestParam(required = true) String patientUuid) throws Exception {
		JSONParser jsonParser = new JSONParser();
		try {
			
			String patientUrl = "https://192.168.19.145/openmrs/ws/rest/v1/patient/"+patientUuid+"?v=full";
			String patientResponse = HttpUtil.get(patientUrl, "", "admin:test");
			//Read JSON file
			JSONObject getPatient = (JSONObject) jsonParser.parse(patientResponse);
			String personUuid = (String) getPatient.get("uuid");
			String uuid = "";
			if (getPatient.containsKey("error")) {
				System.out.println("Not found");
			} else {
				
				uuid = "/" + personUuid;
			}
			String data = getPatientObject(getPatient, personUuid);
			String patientPostUrl = "https://192.168.19.147/openmrs/ws/rest/v1/bahmnicore/patientprofile";
			
			String returnedResult = HttpUtil.post(patientPostUrl, "", data);
			
			SHRExternalPatient externalPatient = new SHRExternalPatient();
			externalPatient.setAction_type("patient");
			externalPatient.setPatient_uuid(personUuid);
			externalPatient.setIs_send_to_central("0");
			externalPatient.setUuid(UUID.randomUUID().toString());
			
			Context.getService(SHRExternalPatientService.class).saveExternalPatient(externalPatient);
			
			return new ResponseEntity<>(returnedResult, HttpStatus.OK);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage().toString(), HttpStatus.OK);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static String getPatientObject(JSONObject obj, String personUuid) {
		
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject extractedPerson = (JSONObject) obj.get("person");
			JSONArray attributes = new JSONArray();
			JSONArray _attributes = new JSONArray();
			_attributes = (JSONArray) extractedPerson.get("attributes");
			attributes = generateAttrubutes(_attributes);
			JSONObject patient = new JSONObject();
			JSONObject personObject = new JSONObject();
			personObject.put("attributes", attributes);
			personObject.put("gender", extractedPerson.get("gender"));
			personObject.put("uuid", personUuid);
			personObject.put("birthdate", extractedPerson.get("birthdate"));
			personObject.put("deathDate", extractedPerson.get("deathDate"));
			personObject.put("causeOfDeath", extractedPerson.get("causeOfDeath"));
			personObject.put("birthtime", extractedPerson.get("birthtime"));
			/********** name section *************/
			JSONArray names = new JSONArray();
			JSONObject preferredName = new JSONObject();
			preferredName = (JSONObject) extractedPerson.get("preferredName");
			PreferredName preName = new Gson().fromJson(preferredName.toString(), PreferredName.class);
			JSONObject name = (JSONObject) jsonParser.parse(new Gson().toJson(preName));
			names.add(name);
			personObject.put("names", names);
			
			JSONArray addresses = new JSONArray();
			
			JSONObject preferredAddress = new JSONObject();
			preferredAddress = (JSONObject) extractedPerson.get("preferredAddress");
			if(preferredAddress != null) {
			JSONObject _preferredAdress = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(
			    preferredAddress.toString(), PersonAddress.class)));
			
			addresses.add(_preferredAdress);
			personObject.put("addresses", addresses);
			}
			
			JSONObject personInfor = new JSONObject();
			personInfor.put("person", personObject);
			
			JSONArray _identifiers = new JSONArray();
			_identifiers = (JSONArray) obj.get("identifiers");
			
			personInfor.put("identifiers", getIdentifiers(_identifiers));
			patient.put("patient", personInfor);
			
			String url = "https://192.168.19.145/openmrs/ws/rest/v1/relationship";
			String relationshipResponse = HttpUtil.get(url, "person=" + personUuid + "&v=full", "admin:test");
			
			JSONObject _relationshipAsObject = (JSONObject) jsonParser.parse(relationshipResponse);
			JSONArray _relationshipArray = (JSONArray) _relationshipAsObject.get("results");
			
			patient.put("relationships", getRelationships(_relationshipArray));
			//patient.put("image", getImage(""));
			String data = patient.toJSONString();
			return data;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	@RequestMapping(value = "/search/fromGlobalServer", method = RequestMethod.GET)
	public ResponseEntity<String> searchPatientFromGlobalServer(@RequestParam(required = true) String patientInformation) throws Exception {
		JSONParser jsonParser = new JSONParser();
		String patientUrl = "https://192.168.19.145"+ patientInformation;
		String patientSearchResponse = HttpUtil.get(patientUrl, "", "admin:test");
		JSONObject getPatient = (JSONObject) jsonParser.parse(patientSearchResponse);
		return new ResponseEntity<>(getPatient.toString(), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	public static JSONArray getIdentifiers(JSONArray _identifiers) {
		
		JSONArray identifiers = new JSONArray();
		_identifiers.forEach(idenf -> {
			JSONObject _identifier = (JSONObject) idenf;
			JSONObject identifier = new JSONObject();
			try {
				identifier.put("identifier", _identifier.get("identifier"));
				identifier.put("preferred", true);
				identifier.put("identifierType", "Patient_Identifier");
				identifiers.add(identifier);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return identifiers;
	}
	
	
	@SuppressWarnings({ "unchecked" })
	public static JSONArray getRelationships(JSONArray _relationships) {
		JSONArray relationships = new JSONArray();
		_relationships.forEach(rela -> {
			JSONObject relationship = new JSONObject();
			JSONObject _relationshp = (JSONObject) rela;
			JSONObject relationShipType;
			try {
				relationShipType = (JSONObject) _relationshp.get("relationshipType");
				JSONObject personBObject = (JSONObject) _relationshp.get("personB");
				String relationshipUuid = (String) relationShipType.get("uuid");
				JSONObject relationshipTypeObject = new JSONObject();
				relationshipTypeObject.put("uuid", relationshipUuid);
				relationship.put("relationshipType", relationshipTypeObject);
				JSONObject personB = new JSONObject();
				personB.put("display", personBObject.get("display"));
				personB.put("uuid", personBObject.get("uuid"));
				relationship.put("personB", personB);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			relationships.add(relationship);
		});
		return relationships;
	}
	
	@SuppressWarnings("unchecked")
	private static JSONArray generateAttrubutes(JSONArray _attributes) {
		JSONArray attributes = new JSONArray();
		_attributes.forEach(attr -> {
			JSONObject _attribute = new JSONObject();
			JSONObject _atrrType = new JSONObject();
			_attribute = (JSONObject) attr;
			JSONObject attribute = new JSONObject();
			JSONObject attributeType = new JSONObject();
			try {
				_atrrType = (JSONObject) _attribute.get("attributeType");
				attributeType.put("uuid", _atrrType.get("uuid"));
				attributeType.put("name", _atrrType.get("display"));
				attribute.put("attributeType", attributeType);
				String valueAsString = (String) _attribute.get("display");
				if (valueAsString != null) {
					String[] values = valueAsString.split("=");
					String value = "";
					if (values.length == 1) {
						value = values[0];
					} else {
						value = values[1];
					}
					attribute.put("value", value);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			attributes.add(attribute);
		});
		return attributes;
	}
	
	@SuppressWarnings("unchecked")
	public void encounter() {

		JSONParser jsonParser = new JSONParser();

		try {
			String getEncounterUrl = "https://192.168.19.145/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/2c1921f4-f54c-4ad4-b852-94d7563fb67d?includeAll=true";
			String patientResponse = HttpUtil.get(getEncounterUrl, "","admin:test");
			// Read JSON file
			System.out.println("json parse starting...........");
			JSONObject obj = (JSONObject) jsonParser.parse(patientResponse);
			JSONArray obs = (JSONArray) obj.get("observations");

			JSONArray obervations = getObservations(obs);

			JSONObject encounter = (JSONObject) jsonParser
					.parse(new Gson().toJson(new Gson().fromJson(
							obj.toString(), Encounter.class)));
			encounter.put("observations", obervations);
			System.out.println(encounter);

		} catch (Exception e) {
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
