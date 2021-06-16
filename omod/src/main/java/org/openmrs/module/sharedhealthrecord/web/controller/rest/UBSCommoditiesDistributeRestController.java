package org.openmrs.module.sharedhealthrecord.web.controller.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.UBSUniqueIdGenerator;
import org.openmrs.module.sharedhealthrecord.api.UBSCommoditiesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/rest/v1/commodities")
@RestController
public class UBSCommoditiesDistributeRestController {
	protected final Log log = LogFactory.getLog(this.getClass());

	@RequestMapping(value = "/geUniqueId", method = RequestMethod.GET)
	public ResponseEntity<String> getId() throws Exception {
	
		String id = generateSquentialId();
		
		return new ResponseEntity<>(id, HttpStatus.OK);
 
	}
	
	
	
	
	
	private String generateSquentialId () {
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String today = dateFormat.format(date);
		
		UBSUniqueIdGenerator ubsUniqueIdGenerator = new UBSUniqueIdGenerator();
		//SHNEslipNoGenerate afterSaveSlip = null;
		synchronized(this) {
			UBSUniqueIdGenerator getLastId = Context.getService(UBSCommoditiesService.class).getLastEntry(today);		        
			log.error("getLastId" + getLastId.getGenerateId());
			ubsUniqueIdGenerator.setGenerateId(0);
			
			ubsUniqueIdGenerator.setDateCreated(new Date());
			if (getLastId.getGenerateId() == 0) {
				ubsUniqueIdGenerator.setGenerateId(0 + 1);
			} else {
				ubsUniqueIdGenerator.setGenerateId(getLastId.getGenerateId() + 1);
			}
			//Context.openSession();
			log.error("generate id " + ubsUniqueIdGenerator.getGenerateId());
			log.error("getDateCreated " + ubsUniqueIdGenerator.getDateCreated());
			log.error("eid " + ubsUniqueIdGenerator.getEid());
			

			  Context.getService(UBSCommoditiesService.class).saveOrUpdate(ubsUniqueIdGenerator);
			//Context.clearSession();
		}
		String serquenceNumber = "";
		String serquenceNumberToString = ubsUniqueIdGenerator.getGenerateId() + "";
		if (serquenceNumberToString.length() == 1) {
			serquenceNumber += "000" + serquenceNumberToString;
		} else if (serquenceNumberToString.length() == 2) {
			serquenceNumber += "00" + serquenceNumberToString;
		} else if (serquenceNumberToString.length() == 3) {
			serquenceNumber += "0" + serquenceNumberToString;
		} else {
			serquenceNumber = serquenceNumberToString;
		}
		
		return serquenceNumber;
	}
	
}
