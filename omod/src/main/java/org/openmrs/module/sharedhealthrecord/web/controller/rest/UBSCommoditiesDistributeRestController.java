package org.openmrs.module.sharedhealthrecord.web.controller.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistributeDetails;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistribution;
import org.openmrs.module.sharedhealthrecord.UBSMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescribedMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescription;
import org.openmrs.module.sharedhealthrecord.UBSUniqueIdGenerator;
import org.openmrs.module.sharedhealthrecord.api.UBSCommoditiesService;
import org.openmrs.module.sharedhealthrecord.api.UBSPrescriptionService;
import org.openmrs.module.sharedhealthrecord.dto.UBSCommoditiesDistributeDetailsDTO;
import org.openmrs.module.sharedhealthrecord.dto.UBSCommoditiesDistributionDTO;
import org.openmrs.module.sharedhealthrecord.dto.UBSCommoditiesReportDTO;
import org.openmrs.module.sharedhealthrecord.dto.UBSPrescribedMedicinesDTO;
import org.openmrs.module.sharedhealthrecord.dto.UBSPrescriptionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;


@RequestMapping("/rest/v1/commodities")
@RestController
public class UBSCommoditiesDistributeRestController {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	Gson gson = new Gson();
	
	
	@RequestMapping(value = "/save-update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> saveCommodities(@RequestBody UBSCommoditiesDistributionDTO dto) throws Exception {
		
		JSONObject response = new JSONObject();
		log.error("DTO" + dto);
		try {
			
			Set<UBSCommoditiesDistributeDetailsDTO> ubsCommoditiesDistributeDetailsDTOs = dto.getUbsCommoditiesDistributeDetailsDto();
			UBSCommoditiesDistribution ubsCommoditiesDistribution = Context.getService(UBSCommoditiesService.class).findByDistributeId(dto.getDistributeId());
			if (ubsCommoditiesDistribution == null) {
				ubsCommoditiesDistribution = new UBSCommoditiesDistribution();
				ubsCommoditiesDistribution.setUuid(UUID.randomUUID().toString());
				ubsCommoditiesDistribution.setDateCreated(new Date());
				ubsCommoditiesDistribution.setCreator(Context.getAuthenticatedUser());
			}
			else {
				ubsCommoditiesDistribution.setChangedBy(Context.getAuthenticatedUser());
				ubsCommoditiesDistribution.setDateChanged(new Date());
			}
			
			ubsCommoditiesDistribution.setPatientName(dto.getPatientName());
			ubsCommoditiesDistribution.setPatientUuid(dto.getPatientUuid());
			ubsCommoditiesDistribution.setGender(dto.getGender());

			ubsCommoditiesDistribution.setPatientAge(dto.getPatientAge());
			ubsCommoditiesDistribution.setProviderName(dto.getProviderName());
			ubsCommoditiesDistribution.setSlipNo(generateSquentialId());
			ubsCommoditiesDistribution.setDistributeDate(dto.getDistributeDate());
	
				
			log.error("ubs distribute Object Creating Seuccess " + dto.getPatientName());
			Set<UBSCommoditiesDistributeDetails> ubsCommoditiesDistributeDetails = new HashSet<UBSCommoditiesDistributeDetails>();;
			for (UBSCommoditiesDistributeDetailsDTO ubsDetailsDTO : ubsCommoditiesDistributeDetailsDTOs) {
				
				UBSCommoditiesDistributeDetails ubsDistributeDetails = Context.getService(UBSCommoditiesService.class).findByDistributeDetailsId(ubsDetailsDTO.getDistributeDetailsId());
				
				if(ubsDistributeDetails == null) {
					ubsDistributeDetails = new UBSCommoditiesDistributeDetails();
					ubsDistributeDetails.setUuid(UUID.randomUUID().toString());
					ubsDistributeDetails.setDateCreated(new Date());
					ubsDistributeDetails.setCreator(Context.getAuthenticatedUser());
				}
				else {
					ubsDistributeDetails.setChangedBy(Context.getAuthenticatedUser());
					ubsDistributeDetails.setDateChanged(new Date());
				}
				
				ubsDistributeDetails.setCommoditiesName(ubsDetailsDTO.getCommoditiesName());
				ubsDistributeDetails.setCommoditiesId(ubsDetailsDTO.getCommoditiesId());
				ubsDistributeDetails.setQuantity(ubsDetailsDTO.getQuantity());

				ubsCommoditiesDistributeDetails.add(ubsDistributeDetails);
			}

			ubsCommoditiesDistribution.setUbsCommoditiesDistributeDetails(ubsCommoditiesDistributeDetails);
			log.error("ubsprescrion Object Creating Seuccess full " + ubsCommoditiesDistribution.getUbsCommoditiesDistributeDetails().size());
			UBSCommoditiesDistribution afterCommoditiesDistribution =  Context.getService(UBSCommoditiesService.class).saveOrUpdate(ubsCommoditiesDistribution);
			response.put("message", "Successfully Saved");
			response.put("prescriptionId", afterCommoditiesDistribution.getDistributeId());
			
		}
		catch (Exception e) {
			e.printStackTrace();
			response.put("message", e.getMessage());
			return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		
	}
	
	
	@RequestMapping(value = "/geUniqueId", method = RequestMethod.GET)
	public ResponseEntity<String> getId() throws Exception {
	
		//String id = generateSquentialId();
		  JSONObject testobject = new JSONObject();
	        testobject.put("tableData", "<tr><th>Name</th></tr>");
		return new ResponseEntity<>(testobject.toString(), HttpStatus.OK);
 
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
		
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		String dayS = day >= 10 ? "" + day : "0" + day;
		String monthS = month >= 10 ? "" + month : "0" + month;
		String sequenceSlipNo = "" + year + monthS + dayS + serquenceNumber;
		
		return sequenceSlipNo;
	}
	
	@RequestMapping(value = "/commoditiesList/{patientUuid}", method = RequestMethod.GET)
	public ResponseEntity<String> getCommoditiesList(@PathVariable String patientUuid) throws Exception {
		try {
			List<UBSCommoditiesReportDTO> reportDto = Context.getService(UBSCommoditiesService.class).findAllByPatientUuid(patientUuid);
			String reportJson = gson.toJson(reportDto);
			return new ResponseEntity<>(reportJson, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@RequestMapping(value = "/save-update-in-global", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> saveCommoditiesForGlobal(@RequestBody UBSCommoditiesDistributionDTO dto) throws Exception {
		
		JSONObject response = new JSONObject();
		log.error("DTO" + dto);
		try {
			UBSCommoditiesDistribution getPreviousData = Context.getService(UBSCommoditiesService.class).findByDistributeUuid(dto.getUuid());
			if(getPreviousData == null) {
			Set<UBSCommoditiesDistributeDetailsDTO> ubsCommoditiesDistributeDetailsDTOs = dto.getUbsCommoditiesDistributeDetailsDto();
			UBSCommoditiesDistribution ubsCommoditiesDistribution = Context.getService(UBSCommoditiesService.class).findByDistributeId(dto.getDistributeId());
			if (ubsCommoditiesDistribution == null) {
				ubsCommoditiesDistribution = new UBSCommoditiesDistribution();
				ubsCommoditiesDistribution.setUuid(dto.getUuid());
				ubsCommoditiesDistribution.setDateCreated(new Date());
				ubsCommoditiesDistribution.setCreator(Context.getAuthenticatedUser());
			}
			else {
				ubsCommoditiesDistribution.setChangedBy(Context.getAuthenticatedUser());
				ubsCommoditiesDistribution.setDateChanged(new Date());
			}
			
			ubsCommoditiesDistribution.setPatientName(dto.getPatientName());
			ubsCommoditiesDistribution.setPatientUuid(dto.getPatientUuid());
			ubsCommoditiesDistribution.setGender(dto.getGender());

			ubsCommoditiesDistribution.setPatientAge(dto.getPatientAge());
			ubsCommoditiesDistribution.setProviderName(dto.getProviderName());
			ubsCommoditiesDistribution.setSlipNo(dto.getSlipNo());
			ubsCommoditiesDistribution.setDistributeDate(dto.getDistributeDate());
	
				
			log.error("ubs distribute Object Creating Seuccess " + dto.getPatientName());
			Set<UBSCommoditiesDistributeDetails> ubsCommoditiesDistributeDetails = new HashSet<UBSCommoditiesDistributeDetails>();;
			for (UBSCommoditiesDistributeDetailsDTO ubsDetailsDTO : ubsCommoditiesDistributeDetailsDTOs) {
				
				UBSCommoditiesDistributeDetails ubsDistributeDetails = Context.getService(UBSCommoditiesService.class).findByDistributeDetailsId(ubsDetailsDTO.getDistributeDetailsId());
				
				if(ubsDistributeDetails == null) {
					ubsDistributeDetails = new UBSCommoditiesDistributeDetails();
					ubsDistributeDetails.setUuid(ubsDetailsDTO.getUuid());
					ubsDistributeDetails.setDateCreated(new Date());
					ubsDistributeDetails.setCreator(Context.getAuthenticatedUser());
				}
				else {
					ubsDistributeDetails.setChangedBy(Context.getAuthenticatedUser());
					ubsDistributeDetails.setDateChanged(new Date());
				}
				
				ubsDistributeDetails.setCommoditiesName(ubsDetailsDTO.getCommoditiesName());
				ubsDistributeDetails.setCommoditiesId(ubsDetailsDTO.getCommoditiesId());
				ubsDistributeDetails.setQuantity(ubsDetailsDTO.getQuantity());

				ubsCommoditiesDistributeDetails.add(ubsDistributeDetails);
			}

			ubsCommoditiesDistribution.setUbsCommoditiesDistributeDetails(ubsCommoditiesDistributeDetails);
			log.error("ubsprescrion Object Creating Seuccess full " + ubsCommoditiesDistribution.getUbsCommoditiesDistributeDetails().size());
			Context.getService(UBSCommoditiesService.class).saveOrUpdate(ubsCommoditiesDistribution);
			response.put("message", "Successfully Saved");
			response.put("status", "SUCCESS");
			}
			else {
				response.put("message", "Already Exist");
				response.put("status", "SUCCESS");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			response.put("message", e.getMessage());
			response.put("status", "FAILED");
			return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		
	}
	
}
