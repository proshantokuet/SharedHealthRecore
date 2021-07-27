package org.openmrs.module.sharedhealthrecord.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistributeDetails;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistribution;

public class UBSCommoditiesDataConverter {

	public static JSONObject toConvert(UBSCommoditiesDistribution ubsCommoditiesDistribution) throws JSONException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		JSONObject ubsDstributionObject = new JSONObject();
		ubsDstributionObject.putOpt("distributeId", 0);
		ubsDstributionObject.putOpt("patientUuid", ubsCommoditiesDistribution.getPatientUuid());
		ubsDstributionObject.putOpt("patientName", ubsCommoditiesDistribution.getPatientName());
		ubsDstributionObject.putOpt("gender",ubsCommoditiesDistribution.getGender());
		ubsDstributionObject.putOpt("patientAge", ubsCommoditiesDistribution.getPatientAge());
		ubsDstributionObject.putOpt("providerName", ubsCommoditiesDistribution.getProviderName());
		ubsDstributionObject.putOpt("slipNo", ubsCommoditiesDistribution.getSlipNo());
		String distributeDate = dateFormat.format(ubsCommoditiesDistribution.getDistributeDate());
		ubsDstributionObject.putOpt("distributeDate", distributeDate);
		ubsDstributionObject.putOpt("uuid", ubsCommoditiesDistribution.getUuid());
		

		Set<UBSCommoditiesDistributeDetails> ubsCommoditiesDistributeDetails = ubsCommoditiesDistribution.getUbsCommoditiesDistributeDetails();
		JSONArray ubsCommodetailsArray = new JSONArray();
		for (UBSCommoditiesDistributeDetails ubsDetails : ubsCommoditiesDistributeDetails) {
			JSONObject ubsCommoditiesDetailsObj = new JSONObject();
			ubsCommoditiesDetailsObj.putOpt("distributeDetailsId",0);
			ubsCommoditiesDetailsObj.putOpt("commoditiesName", ubsDetails.getCommoditiesName());
			ubsCommoditiesDetailsObj.putOpt("commoditiesId", ubsDetails.getCommoditiesId());
			ubsCommoditiesDetailsObj.putOpt("quantity", ubsDetails.getQuantity());
			ubsCommoditiesDetailsObj.putOpt("uuid", ubsDetails.getUuid());
			ubsCommodetailsArray.put(ubsCommoditiesDetailsObj);
		}
		
		ubsDstributionObject.putOpt("ubsCommoditiesDistributeDetailsDto", ubsCommodetailsArray);
		
		return ubsDstributionObject;
		
	}
}
