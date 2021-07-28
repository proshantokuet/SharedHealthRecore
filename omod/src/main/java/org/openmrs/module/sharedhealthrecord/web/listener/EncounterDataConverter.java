package org.openmrs.module.sharedhealthrecord.web.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.module.sharedhealthrecord.domain.GroupMember;
import org.openmrs.module.sharedhealthrecord.domain.GroupMemberWithValue;
import org.openmrs.module.sharedhealthrecord.domain.Observation;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithGroupMemebrs;
import org.openmrs.module.sharedhealthrecord.domain.ObservationWithValues;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.module.sharedhealthrecord.utils.ServerAddress;

import com.google.gson.Gson;

public class EncounterDataConverter {
	
	private final static String baseOpenmrsUrl = "https://localhost";
	private final static String globalServerUrl = ServerAddress.globalServerUrl;

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
				String conceptName = "";
				if(ob.containsKey("conceptNameToDisplay")) {
					 conceptName = (String) ob.get("conceptNameToDisplay");
				}
				if(conceptName.equalsIgnoreCase("Vitals") || conceptName.equalsIgnoreCase("History and Examination")) {
					
				}
				else {
				if (!StringUtils.isBlank(type) && type.equalsIgnoreCase("Coded")) {
					
					JSONObject obs = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(ob.toString(),
					    ObservationWithValues.class)));
					observations.add(obs);
				} else if (groupMembers.size() != 0) {
					
					JSONObject obs = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(ob.toString(),
					    ObservationWithGroupMemebrs.class)));
					JSONArray groupMembersArray = new JSONArray();
					for (int i = 0; i < groupMembers.size(); i++) {
						
						JSONObject groupMemberCustom = (JSONObject) groupMembers.get(i);
						JSONObject mappedGroupmemberObject = new JSONObject();
						try {
							String typeGroupMember = (String) groupMemberCustom.get("type");
							if(typeGroupMember.equalsIgnoreCase("Coded")) {
							 mappedGroupmemberObject = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(groupMemberCustom.toString(),
									 GroupMemberWithValue.class)));
							}
							else
							{
							 mappedGroupmemberObject = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(groupMemberCustom.toString(),
									 GroupMember.class)));
							}
							groupMembersArray.add(mappedGroupmemberObject);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					obs.put("groupMembers", groupMembersArray);
					observations.add(obs);
				} else {
					
					JSONObject obs = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(ob.toString(),
					    Observation.class)));
					observations.add(obs);
				}
				
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
	public static Boolean createVisit (JSONObject obj, String patientUuid) {
		
		Boolean visitSavingResponse = false;
		JSONParser jsonParser = new JSONParser();
		
		try {
			
			String visitUUIdString = (String)obj.get("visitUuid");
			String visitDetailsByVIsitUuidURL = globalServerUrl + "/openmrs/ws/rest/v1/save-Patient/search/patientVisitByUuid?visit_uuid=" + visitUUIdString;
			String visitDetailsByVIsitUuid = HttpUtil.get(visitDetailsByVIsitUuidURL, "", "admin:test");
			JSONObject visitObject = (JSONObject) jsonParser.parse(visitDetailsByVIsitUuid);
			visitObject.put("patient_uuid", patientUuid);
			visitObject.remove("isFound");
			String visitSavingUrl = baseOpenmrsUrl + "/openmrs/ws/rest/v1/save-Patient/insert/patientVisitDetails";
			String visitSavingResponseString = HttpUtil.post(visitSavingUrl, "", visitObject.toString());
			JSONObject visitSavingObject = (JSONObject) jsonParser.parse(visitSavingResponseString);
			if(visitSavingObject.containsKey("isSuccessfull")) {
				Boolean isSuccessfull =  (Boolean)visitSavingObject.get("isSuccessfull");
				if(isSuccessfull) {
					visitSavingResponse = true;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			visitSavingResponse = false;
		}
		return visitSavingResponse;
	}
	
	public static final Map<String, String> visitTypeMapping = new HashMap<String, String>();
	static {
        visitTypeMapping.put("c228eab1-3f10-11e4-adec-0800271c1b75", "IPD");
        visitTypeMapping.put("c22a5000-3f10-11e4-adec-0800271c1b75", "OPD");
        visitTypeMapping.put("bef32e14-3f12-11e4-adec-0800271c1b75", "LAB VISIT");
	}
}
