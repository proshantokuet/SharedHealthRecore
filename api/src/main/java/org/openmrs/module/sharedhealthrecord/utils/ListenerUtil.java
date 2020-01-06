package org.openmrs.module.sharedhealthrecord.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ListenerUtil {
	public static void deleteEncounter(String encounterUuid,String serverAddress){
		String deleteWithoutPurge = 
				serverAddress+"openmrs/ws/rest/v1/encounter/"+encounterUuid;
		String deleteFirst = HttpUtil.delete(deleteWithoutPurge, "", "admin:test");
		//delete encounter
//		errorLogInsert("Error Log Delete ",deleteFirst,encounterUuid,0);
		String deleteUrlString = 
				serverAddress+"openmrs/ws/rest/v1/encounter/"+encounterUuid
		+"?purge=true";
		String result = HttpUtil.delete(deleteUrlString, "", "admin:test");
//		return true;
	}
	
	public static String createVisit(JSONObject obj,String patientUuid,String serverAddress) throws JSONException{
		String visitSavingResponse = "";
		obj.remove("isFound");
		obj.put("patient_uuid", patientUuid);
		try {
			
			String visitSavingUrl = serverAddress + "openmrs/ws/rest/v1/save-Patient/insert/patientVisitDetails";			
			visitSavingResponse = HttpUtil.post(visitSavingUrl, "", obj.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return visitSavingResponse;
	}
}
