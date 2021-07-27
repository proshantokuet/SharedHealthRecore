package org.openmrs.module.sharedhealthrecord.web.listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistribution;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.openmrs.module.sharedhealthrecord.api.SHRExternalPatientService;
import org.openmrs.module.sharedhealthrecord.api.UBSCommoditiesService;
import org.openmrs.module.sharedhealthrecord.domain.Encounter;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;
import org.openmrs.module.sharedhealthrecord.utils.HttpUtil;
import org.openmrs.module.sharedhealthrecord.utils.ServerAddress;
import org.openmrs.module.sharedhealthrecord.utils.UBSCommoditiesDataConverter;
import org.openmrs.module.sharedhealthrecord.web.controller.rest.SharedHealthRecordManageRestController;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@EnableScheduling
@Configuration
@EnableAsync
@Controller
public class UBSCommoditiesListener{
	
	String localServer = ServerAddress.localServer();
	String centralServer = ServerAddress.centralServer();
	String isDeployInGlobal = ServerAddress.isDeployInGlobal;
	public static DateFormat dateFormatTwentyFourHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final ReentrantLock lock = new ReentrantLock();
	
	@SuppressWarnings("rawtypes")
//	@Scheduled(fixedRate=10000)
	private static final Logger log = LoggerFactory.getLogger(UBSCommoditiesListener.class);
	public void sendAllData() throws Exception {
		log.error("Entered in commodities listener" + new Date());
		if (!lock.tryLock()) {
			log.error("It is already in progress.");
	        return;
		}
		log.error("isDeployInGlobal " + isDeployInGlobal);
		if(isDeployInGlobal.equalsIgnoreCase("0")) {
			Context.openSession();
			
			JSONObject getResponse = null;
			boolean status = true;
			try{
				String globalServerUrl = centralServer + "openmrs/ws/rest/v1/visittype";
				String get_result = HttpUtil.get(globalServerUrl, "", "admin:test");
				JSONObject patienResponseCheck = new JSONObject(get_result);			
			}catch(Exception e){
				e.printStackTrace();
				status = false;
			}
			
			if(status){
				try {
					sendCommodities();
					sendFailedCommodities();
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					lock.unlock();
					log.error("complete listener commodities  at:" +new Date());
				}
			}
			
			Context.closeSession();
		}
	}
	
	
	
	
	
	
	
	public void sendCommodities(){
		JSONParser jsonParser = new JSONParser();
		// Check shr_action_audit_info for last sent timestamp
		String last_entry = Context.getService(SHRActionAuditInfoService.class).getLastEntryByType("CommoditiesID");
		List<UBSCommoditiesDistribution> commoditiesDistributions = Context.getService(UBSCommoditiesService.class).getAllDistributionList(Integer.parseInt(last_entry));
			for(UBSCommoditiesDistribution receipt: commoditiesDistributions) {
				try{

						String mid = Integer.toString(receipt.getDistributeId());
						// 0 is the value of voided Status in case of failure in error log table
						Boolean saveFlagMoneyReceipt = CommditiesFetchAndPost(receipt,0);
				}catch(Exception e){
					String midEx = Integer.toString(receipt.getDistributeId());
					errorLogInsert("Commodities",e.toString(),midEx,0,"");
				}
			}
	}
	public synchronized void sendFailedCommodities(){
		List<SHRActionErrorLog> failedReceipts = Context.getService(SHRActionErrorLogService.class)
				.get_list_by_Action_type("Commodities");
//		errorLogUpdate("Money Receipt List","list size check",Integer.toString(failedReceipts.size()));
//		errorLogUpdate("Loop Starts","Loop Starts",UUID.randomUUID().toString());
		for(SHRActionErrorLog receipt: failedReceipts){
			boolean status = true;
			try{
				String globalServerUrl = centralServer + "openmrs/ws/rest/v1/visittype";
				String get_result = HttpUtil.get(globalServerUrl, "", "admin:test"); 
				
			}catch(Exception e){
				e.printStackTrace();
				status = false;
			}
			if(status) {
			String mid = receipt.getUuid();
			Boolean flag = false;
//			errorLogUpdate("Money Receipt Test","Status:"+receipt.getSent_status(),mid);
				if(receipt.getSent_status() == 0){
					UBSCommoditiesDistribution commoditiesDistributions = Context.getService(UBSCommoditiesService.class).findByDistributeId(Integer.parseInt(mid));
					int val = receipt.getVoided()+1;
					Boolean sentFlag = CommditiesFetchAndPostForFailed(commoditiesDistributions, val > 1 ? 2 : val);
					log.error("sentFlag money receipt" + sentFlag);
					Context.getService(SHRActionErrorLogService.class).
						updateSentStatus(receipt.getEid(), sentFlag == true? 1 : 0);
					
				}
			}
		}
	}
	
	private Boolean CommditiesFetchAndPost(UBSCommoditiesDistribution distribution,int voidedStatus){
		String distributeId = String.valueOf(distribution.getDistributeId());
		try{
			JSONObject convertedCommodities = UBSCommoditiesDataConverter.toConvert(distribution);
			log.error("convertedCommodities" + convertedCommodities.toString());
			String centralPostUrl = centralServer+"openmrs/ws/rest/v1/commodities/save-update-in-global";
			log.error("centralPostUrl" + centralPostUrl);
			String postAction = HttpUtil.post(centralPostUrl, "", convertedCommodities.toString());
			log.error("postAction" + postAction);
			JSONObject postFollowUpResult = new JSONObject(postAction);
			if(postFollowUpResult.has("status")) {
				String isSuccess = postFollowUpResult.getString("status");
				if(isSuccess.equalsIgnoreCase("SUCCESS")) {
					SaveStatusOfEachOnSync("Commodities","Success", distributeId);
					String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
							.updateAuditInfoByType(distributeId, "CommoditiesID");
				}
				else if(isSuccess.equalsIgnoreCase("FAILED")) {
					 errorLogInsert("Commodities","Commodities Sync Failed From Local To Global:"+ postAction,distributeId,2,postAction);
						String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
								.updateAuditInfoByType(distributeId, "CommoditiesID");
				}
			}
		}catch(Exception e){
			log.error("in try catch after failed to post money receipt" + distributeId);
			if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
				errorLogInsert("Commodities",e.toString(),distributeId,voidedStatus == 2 ? 1 : voidedStatus,"");
			}
			else {
				errorLogInsert("Commodities",e.toString(),distributeId,voidedStatus,"");
			}
			String audit_info_save = Context.getService(SHRActionAuditInfoService.class)
					.updateAuditInfoByType(distributeId, "CommoditiesID");
			return false;
		}
		log.error("in the verge of returning status " + distributeId);
		return true;
	}
	//<param>
		//patientUuid - patient's identity key.
		//id - of event records - for updating last index of a table.
		//failedPatient - flag to check which kind of encounter it is.
	//</param>
	
	
	
	public void errorLogInsert(String action_type,String message,String uuId,Integer voided,String postJson){
		Context.clearSession();
		Context.openSession();
		//Delete existing if void > 0
//		if(voided > 0) {
//			Context.getService(SHRActionErrorLogService.class)
//				.delete_by_type_and_uuid(action_type, uuId);
//		}
		//Insert Log
		SHRActionErrorLog log  = Context.getService(SHRActionErrorLogService.class).getErrorByActionTypeAndIdWithSentStatus(action_type, uuId);
		if(log == null) {
			log = new SHRActionErrorLog();
		}
		log.setAction_type(action_type);
		log.setError_message(message);
		log.setUuid(uuId);
		log.setVoided(voided);
		//Insert will be called on exception 
		//So 0 - will be inserted automatically
		log.setSent_status(0);
		Context.getService(SHRActionErrorLogService.class)
			.insertErrorLog(log);
		Context.clearSession();
		Context.openSession();
	}
	
	
	public void SaveStatusOfEachOnSync(String action_type,String message,String uuId){
		Context.clearSession();
		Context.openSession();
		SHRActionErrorLog log = new SHRActionErrorLog();
		log.setAction_type(action_type);
		log.setError_message(message);
		log.setUuid(uuId);
		log.setVoided(0);
		log.setSent_status(1);
		Context.getService(SHRActionErrorLogService.class)
			.insertErrorLog(log);
		Context.clearSession();
		Context.openSession();
	}
	

	
	//Money Receipt Get to Post JSON Converter
	private Boolean CommditiesFetchAndPostForFailed(UBSCommoditiesDistribution distribution,int voidedStatus){
		String distributeId = String.valueOf(distribution.getDistributeId());
		boolean flag = true;
		try{
			JSONObject convertedCommodities = UBSCommoditiesDataConverter.toConvert(distribution);
			String centralPostUrl = centralServer+"openmrs/ws/rest/v1/commodities/save-update-in-global";
			String postAction = HttpUtil.post(centralPostUrl, "", convertedCommodities.toString());
			JSONObject postFollowUpResult = new JSONObject(postAction);
			if(postFollowUpResult.has("status")) {
				String isSuccess = postFollowUpResult.getString("status");
				if(isSuccess.equalsIgnoreCase("SUCCESS")) {
					flag = true;
				}
				else if(isSuccess.equalsIgnoreCase("FAILED")) {
					flag = false;
				}
			}
		}catch(Exception e){
			log.error("in try catch after failed to post money receipt" + distributeId);
			if("java.lang.RuntimeException: java.net.ConnectException: Network is unreachable (connect failed)".equalsIgnoreCase(e.toString())) {
				errorLogInsert("Commodities",e.toString(),distributeId,voidedStatus == 2 ? 1 : voidedStatus,"");
			}
			else {
				errorLogInsert("Commodities",e.toString(),distributeId,voidedStatus,"");
			}
			
			return false;
		}
		log.error("in the verge of returning status " + distributeId);
		return flag;
	}

	
}
