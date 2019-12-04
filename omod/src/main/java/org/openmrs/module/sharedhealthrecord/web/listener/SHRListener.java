package org.openmrs.module.sharedhealthrecord.web.listener;

import org.json.JSONObject;
import org.openmrs.module.sharedhealthrecord.api.SHRActionAuditInfoService;
import org.openmrs.module.sharedhealthrecord.api.SHRActionErrorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;


@Service
@EnableScheduling
@Configuration
@EnableAsync
@Controller
public class SHRListener {

	
	@SuppressWarnings("rawtypes")
	public void sendData() throws Exception {
		JSONObject getResponse = null;
		boolean status = true;
		
		try{
			
		}catch(Exception e){
			status = false;
		}
		
		if(status){
			try{
				sendFailedPatient();
			}catch(Exception e){
				
			}
			try{
				sendPatient();
			}catch(Exception e){
				
			}
			try{
				sendFailedEncounter();
			}catch(Exception e){
				
			}
			try{
				sendEncounter();
			}catch(Exception e){
	
			}
			try{
				sendFailedMoneyReceipt();
			}catch(Exception e){
	
			}
			try{
				sendMoneyReceipt();
			}catch(Exception e){
	
			}
		}
		
	}
	
	public void sendPatient(){
		
	}
	public void sendFailedPatient(){
		
	}
	public void sendEncounter(){
		
	}
	public void sendFailedEncounter(){
		
	}
	public void sendMoneyReceipt(){
		
	}
	public void sendFailedMoneyReceipt(){
		
	}
}
