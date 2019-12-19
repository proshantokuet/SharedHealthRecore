package org.openmrs.module.sharedhealthrecord.web.controller.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openmrs.api.context.Context;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.SHRPatientOrigin;
import org.openmrs.module.sharedhealthrecord.SHRPatientVisit;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;
import org.openmrs.module.sharedhealthrecord.api.SHRPatientOriginService;
import org.openmrs.module.sharedhealthrecord.api.SHRPatientVisitService;
import org.openmrs.module.sharedhealthrecord.domain.Encounter;
import org.openmrs.module.sharedhealthrecord.domain.Observation;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithGroupMemebrs;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithValues;
import org.openmrs.module.sharedhealthrecord.domain.PersonAddress;
import org.openmrs.module.sharedhealthrecord.domain.PreferredName;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@RequestMapping("/rest/v1/save-Patient")
@RestController
public class SharedHealthRecordManageRestController {
	
	private final static String baseOpenmrsUrl = "https://192.168.19.147";
	
	private final static String globalServerUrl = "https://192.168.19.145";
	
	public static DateFormat dateFormatTwentyFourHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@RequestMapping(value = "/patient/toLocalServer", method = RequestMethod.GET)
	public ResponseEntity<String> getPatientFromGlobalServer(@RequestParam(required = true) String patientUuid,@RequestParam(required = true) String loginLocationUuid) throws Exception {
		JSONParser jsonParser = new JSONParser();
		try {
			
			String patientUrl = globalServerUrl + "/openmrs/ws/rest/v1/patient/"+patientUuid+"?v=full";
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
			String patientPostUrl = baseOpenmrsUrl + "/openmrs/ws/rest/v1/bahmnicore/patientprofile";
			
			String returnedResult = HttpUtil.post(patientPostUrl, "", data);
			savePatientEntryDetails("patient", personUuid);
			
			Boolean isCompletedSavingBoolean = encounter(patientUuid, loginLocationUuid);
			if (isCompletedSavingBoolean) {
				savePatientEntryDetails("encounter", personUuid);
			}
			return new ResponseEntity<>(returnedResult, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage().toString(), HttpStatus.OK);
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/patient/updatePatientEntryDetails", method = RequestMethod.GET)
	public ResponseEntity<String> UpdatePatientEntryDetails(@RequestParam(required = true) String patientUuid) throws Exception {
		try {
			SHRExternalPatient shrExternalPatient = Context.getService(SHRExternalPatientService.class).findExternalPatientByPatientUUid(patientUuid);
			if(shrExternalPatient != null) {
				shrExternalPatient.setIs_send_to_central("1");
				Context.getService(SHRExternalPatientService.class).saveExternalPatient(shrExternalPatient);
				JSONObject responseoJsonObject = new JSONObject();
				responseoJsonObject.put("patientUuid", patientUuid);
				responseoJsonObject.put("isSuccessfull", true);
				return new ResponseEntity<>(responseoJsonObject.toJSONString(), HttpStatus.OK);
			}
			else {
				JSONObject responseoJsonObject = new JSONObject();
				responseoJsonObject.put("patientUuid", patientUuid);
				responseoJsonObject.put("isSuccessfull", false);
				return new ResponseEntity<>(responseoJsonObject.toJSONString(), HttpStatus.OK);
			}
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
			
			String url = globalServerUrl + "/openmrs/ws/rest/v1/relationship";
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
		String patientUrl = globalServerUrl + patientInformation;
		String patientSearchResponse = HttpUtil.get(patientUrl, "", "admin:test");
		JSONObject getPatient = (JSONObject) jsonParser.parse(patientSearchResponse);
		return new ResponseEntity<>(getPatient.toString(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/insert/patientOriginDetails", method = RequestMethod.GET)
	public ResponseEntity<String> savePatientOriginDetails(@RequestParam(required = true) String patient_uuid,@RequestParam(required = true) String patient_origin) throws Exception {
		SHRPatientOrigin shrpatientorigin = new SHRPatientOrigin();
		shrpatientorigin.setPatient_uuid(patient_uuid);
		shrpatientorigin.setPatient_origin(patient_origin);
		SHRPatientOrigin shrpatientoriginresponse = Context.getService(SHRPatientOriginService.class).savePatientOrigin(shrpatientorigin);
		return new ResponseEntity<>(shrpatientoriginresponse.toString(), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/search/patientOriginByUuid", method = RequestMethod.GET)
	public ResponseEntity<String> savePatientOriginDetails(@RequestParam(required = true) String patient_uuid) throws Exception {
		SHRPatientOrigin shrpatientoriginresponse = Context.getService(SHRPatientOriginService.class).getpatientOriginByPatientuuid(patient_uuid);
		if (shrpatientoriginresponse != null) {
			JSONObject patientObject = new JSONObject();
			patientObject.put("Origin_ID", shrpatientoriginresponse.getOriginId());
			patientObject.put("Patient_uuid", shrpatientoriginresponse.getPatient_uuid());
			patientObject.put("Patient_origin", shrpatientoriginresponse.getPatient_origin());
			return new ResponseEntity<>(patientObject.toJSONString(), HttpStatus.OK);
		}
		else {
			String message = "No patient Found";
			return new ResponseEntity<>(new Gson().toJson(message), HttpStatus.OK);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/insert/patientVisitDetails", method = RequestMethod.POST)
	public ResponseEntity<String> savePatientVisitDetails(@RequestBody String visitJson) throws Exception {

		JSONParser jsonParser = new JSONParser();
		JSONObject patientVisitJsonObject = (JSONObject) jsonParser.parse(visitJson);
		SHRPatientVisit shrPatientVisit = new SHRPatientVisit();
		if (patientVisitJsonObject.containsKey("patient_uuid")) {
			String patient_id = (String) patientVisitJsonObject.get("patient_uuid");
			SHRPatientVisit patientIdResponse = Context.getService(SHRPatientVisitService.class).getPatientIdByPatientUuid(patient_id);
			if (patientIdResponse != null) {
				
				shrPatientVisit.setPatient_id(patientIdResponse.getPerson_id());
			}
			else {
				JSONObject patientObject = new JSONObject();
				patientObject.put("isFound", false);
				patientObject.put("message", "No patient Found by this uuid");
				return new ResponseEntity<>(patientObject.toJSONString(), HttpStatus.OK);
			}
			
		}
		if (patientVisitJsonObject.containsKey("visit_type_id")) {
			String visit_type_id =  (String) patientVisitJsonObject.get("visit_type_id");
			shrPatientVisit.setVisit_type_id(Integer.parseInt(visit_type_id));
		}
		if (patientVisitJsonObject.containsKey("uuid")) {
			shrPatientVisit.setUuid((String) patientVisitJsonObject.get("uuid"));
		}
		if (patientVisitJsonObject.containsKey("date_started")) {
			String daeStarted = (String) patientVisitJsonObject.get("date_started");
			shrPatientVisit.setDate_started(dateFormatTwentyFourHour.parse(daeStarted));
		}
		if (patientVisitJsonObject.containsKey("date_stopped")) {
			String dateStopped = (String) patientVisitJsonObject.get("date_stopped");
			shrPatientVisit.setDate_stopped(dateFormatTwentyFourHour.parse(dateStopped));
		}
		if (patientVisitJsonObject.containsKey("location_id")) {
			shrPatientVisit.setLocation_id(1);
		}
		SHRPatientVisit shrpatientoriginresponse = Context.getService(SHRPatientVisitService.class).savePatientVisit(shrPatientVisit);
		JSONObject patientVisitObject = new JSONObject();
		patientVisitObject.put("visit_type_id", Integer.toString(shrpatientoriginresponse.getVisit_type_id()));
		patientVisitObject.put("date_started", shrpatientoriginresponse.getDate_started().toString());
		patientVisitObject.put("date_stopped", shrpatientoriginresponse.getDate_stopped().toString());
		patientVisitObject.put("location_id", Integer.toString(shrpatientoriginresponse.getLocation_id()));
		patientVisitObject.put("patient_id", Integer.toString(shrpatientoriginresponse.getPatient_id()));
		patientVisitObject.put("uuid", shrpatientoriginresponse.getUuid());
		patientVisitObject.put("isSuccessfull", shrpatientoriginresponse.isSuccessfull());
		return new ResponseEntity<>(patientVisitObject.toJSONString(), HttpStatus.OK);
	}
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																					
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/search/patientVisitByUuid", method = RequestMethod.GET)
	public ResponseEntity<String> getPatientVisitDetails(@RequestParam(required = true) String visit_uuid) throws Exception {
		SHRPatientVisit shrpatientVisitresponse = Context.getService(SHRPatientVisitService.class).getPatientVisitByVisitUuid(visit_uuid);
		if (shrpatientVisitresponse != null) {
			JSONObject patientVisitObject = new JSONObject();
			patientVisitObject.put("visit_type_id", Integer.toString(shrpatientVisitresponse.getVisit_type_id()));
			patientVisitObject.put("date_started", shrpatientVisitresponse.getDate_started().toString());
			patientVisitObject.put("date_stopped", shrpatientVisitresponse.getDate_stopped().toString());
			patientVisitObject.put("location_id", Integer.toString(shrpatientVisitresponse.getLocation_id()));
			patientVisitObject.put("patient_id", Integer.toString(shrpatientVisitresponse.getPatient_id()));
			patientVisitObject.put("uuid", shrpatientVisitresponse.getUuid());
			patientVisitObject.put("isFound", true);
			return new ResponseEntity<>(patientVisitObject.toJSONString(), HttpStatus.OK);
		}
		else {
			JSONObject patientVisitObject = new JSONObject();
			patientVisitObject.put("isFound", false);
			patientVisitObject.put("message", "No Visit Found");
			return new ResponseEntity<>(patientVisitObject.toJSONString(), HttpStatus.OK);
		}
		
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
	public  Boolean encounter(String patientUuidString, String loginLocationUuid) {

		JSONParser jsonParser = new JSONParser();
		Boolean postResponseOfService = true ;
		try {
	        
		String patientVisitUrl = globalServerUrl + "/openmrs/ws/rest/v1/visit?includeInactive=true&patient=" + patientUuidString + "&v=custom:(encounters:(uuid))";
		String patientVisitResponse = HttpUtil.get(patientVisitUrl, "", "admin:test");
		JSONObject patientVisitObject = (JSONObject) jsonParser.parse(patientVisitResponse);
		JSONArray visitsArray = (JSONArray) patientVisitObject.get("results");
		if(visitsArray.size() < 1) {
			postResponseOfService = false;
		}
	    visitsArray.forEach(_ob -> {
	    	
			JSONObject encountersObject = (JSONObject) _ob;
			JSONArray encountersArray = (JSONArray) encountersObject.get("encounters");
			encountersArray.forEach(_enc -> {
				
				JSONObject singleEncountersObject = (JSONObject) _enc;
				String encounterUuid = (String) singleEncountersObject.get("uuid");
				String getEncounterUrl = globalServerUrl +"/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/"+encounterUuid+"?includeAll=true";
				String patientencounterResponse = HttpUtil.get(getEncounterUrl, "", "admin:test");
				JSONObject obj;

				try {
					obj = (JSONObject) jsonParser.parse(patientencounterResponse);
					
					String visitSavingResponse = createVisit(obj,loginLocationUuid);
					JSONObject visitJsonAfterSaving = (JSONObject) jsonParser.parse(visitSavingResponse);
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
					String patientServiceUrl = baseOpenmrsUrl + "/openmrs/ws/rest/v1/bahmnicore/bahmniencounter";
					String postResponse = HttpUtil.post(patientServiceUrl, "", encounter.toJSONString());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
	     });
		}
		catch (Exception e) {
			e.printStackTrace();
			postResponseOfService = false;
		}
		return postResponseOfService;
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
	
	@SuppressWarnings("unchecked")
	public static String createVisit (JSONObject obj, String locationUuid) {
		
		String visitSavingResponse = "";
		JSONParser jsonParser = new JSONParser();
		
		try {
			
			String visitUUIdString = (String)obj.get("visitUuid");
			String visitDetailsByVIsitUuidURL = globalServerUrl + "/openmrs/ws/rest/v1/visit/"+visitUUIdString+"?includeAll=true";
			String visitDetailsByVIsitUuid = HttpUtil.get(visitDetailsByVIsitUuidURL, "", "admin:test");
			JSONObject visitObject = (JSONObject) jsonParser.parse(visitDetailsByVIsitUuid);
			JSONObject visitStartJsonObject = new JSONObject();
			visitStartJsonObject.put("visitType", visitObject.get("visitType"));
			visitStartJsonObject.put("patient", visitObject.get("patient"));
			visitStartJsonObject.put("startDatetime", visitObject.get("startDatetime"));
			visitStartJsonObject.put("stopDatetime", visitObject.get("stopDatetime"));
			visitStartJsonObject.put("location", locationUuid);
			String visitSavingUrl = baseOpenmrsUrl + "/openmrs/ws/rest/v1/visit";
			visitSavingResponse = HttpUtil.post(visitSavingUrl, "", visitStartJsonObject.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return visitSavingResponse;
	}
	
	public static final Map<String, String> visitTypeMapping = new HashMap<String, String>();
	static {
        visitTypeMapping.put("c228eab1-3f10-11e4-adec-0800271c1b75", "IPD");
        visitTypeMapping.put("c22a5000-3f10-11e4-adec-0800271c1b75", "OPD");
        visitTypeMapping.put("bef32e14-3f12-11e4-adec-0800271c1b75", "LAB VISIT");
	}
	
	private void savePatientEntryDetails(String actionType, String personUuid) {
		SHRExternalPatient externalPatient = new SHRExternalPatient();
		externalPatient.setAction_type(actionType);
		externalPatient.setPatient_uuid(personUuid);
		externalPatient.setIs_send_to_central("0");
		externalPatient.setUuid(UUID.randomUUID().toString());
		Context.getService(SHRExternalPatientService.class).saveExternalPatient(externalPatient);
	}
}
