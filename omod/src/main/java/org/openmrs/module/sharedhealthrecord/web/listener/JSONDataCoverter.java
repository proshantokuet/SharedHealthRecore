package org.openmrs.module.sharedhealthrecord.web.listener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.module.sharedhealthrecord.domain.PersonAddress;
import org.openmrs.module.sharedhealthrecord.domain.PreferredName;

import com.google.gson.Gson;

public class JSONDataCoverter {

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
			
			JSONArray _relationshipArray = new JSONArray();
			
			patient.put("relationships", getRelationships(_relationshipArray));
			//patient.put("image", getImage(""));
			String data = patient.toJSONString();
			return data;
		}
		catch (Exception e) {
			return null;
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
				identifier.put("preferred", _identifier.get("preferred"));
				if(_identifier.containsKey("identifierType")) {
				JSONObject identifierType = (JSONObject)_identifier.get("identifierType");
				identifier.put("identifierType", identifierType.get("display"));
				}
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
}
