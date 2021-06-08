package org.openmrs.module.sharedhealthrecord.web.controller.rest;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.UBSMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescribedMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescription;
import org.openmrs.module.sharedhealthrecord.api.UBSPrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@RequestMapping("/rest/v1/prescription")
@RestController
public class UBSPrescriptionRestController {
	Gson gson = new Gson();
	//public static DateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
	protected final Log log = LogFactory.getLog(this.getClass());

	@RequestMapping(value = "/save-update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> savePrescription(@RequestBody UBSPrescription dto) throws Exception {
		
		JSONObject response = new JSONObject();
		log.error("DTO" + dto);
		try {
			
			Set<UBSPrescribedMedicines> UbsPrescribedMedicines = dto.getPrescribedMedicine();
			UBSPrescription ubsPrescription = Context.getService(UBSPrescriptionService.class).findById(dto.getPrescriptionId());
			if (ubsPrescription == null) {
				ubsPrescription = new UBSPrescription();
				ubsPrescription.setUuid(UUID.randomUUID().toString());
				ubsPrescription.setDateCreated(new Date());
				ubsPrescription.setCreator(Context.getAuthenticatedUser());
			}
			else {
				ubsPrescription.setChangedBy(Context.getAuthenticatedUser());
				ubsPrescription.setDateChanged(new Date());
			}
			
			ubsPrescription.setPatientName(dto.getPatientName());
			ubsPrescription.setPatientUuid(dto.getPatientUuid());
			ubsPrescription.setGender(dto.getGender());
			ubsPrescription.setVisitDate(dto.getVisitDate());
			ubsPrescription.setVisitUuid(dto.getVisitUuid());
			ubsPrescription.setPatientAge(dto.getPatientAge());
			ubsPrescription.setProviderName(dto.getProviderName());
			ubsPrescription.setProviderInfo(dto.getProviderInfo());
			ubsPrescription.setChiefComplaint(dto.getChiefComplaint());
			ubsPrescription.setDiagnosis(dto.getDiagnosis());
			ubsPrescription.setAdvice(dto.getAdvice());
				
			log.error("ubsprescrion Object Creating Seuccess " + dto.getPatientName());
			Set<UBSPrescribedMedicines> ubsPrescribedMedicinesNew = new HashSet<UBSPrescribedMedicines>();;
			for (UBSPrescribedMedicines ubsMedicine : UbsPrescribedMedicines) {
				
				UBSPrescribedMedicines prescribedMedicine = Context.getService(UBSPrescriptionService.class).findPrescribedMedicineById(ubsMedicine.getPmId());
				
				if(prescribedMedicine == null) {
					prescribedMedicine = new UBSPrescribedMedicines();
					prescribedMedicine.setUuid(UUID.randomUUID().toString());
					prescribedMedicine.setDateCreated(new Date());
					prescribedMedicine.setCreator(Context.getAuthenticatedUser());
				}
				else {
					prescribedMedicine.setChangedBy(Context.getAuthenticatedUser());
					prescribedMedicine.setDateChanged(new Date());
				}
				
				prescribedMedicine.setMedicineName(ubsMedicine.getMedicineName());
				prescribedMedicine.setMedicineId(ubsMedicine.getMedicineId());
				prescribedMedicine.setFrequency(ubsMedicine.getFrequency());
				prescribedMedicine.setDuration(ubsMedicine.getDuration());
				prescribedMedicine.setInstruction(ubsMedicine.getInstruction());
				ubsPrescribedMedicinesNew.add(prescribedMedicine);
			}

			ubsPrescription.setPrescribedMedicine(ubsPrescribedMedicinesNew);
			
			UBSPrescription afterSavePrescription =  Context.getService(UBSPrescriptionService.class).saveorUpdate(ubsPrescription);
			response.put("message", "Prescription Successfully Saved");
			response.put("prescriptionId", afterSavePrescription.getPrescriptionId());
			
		}
		catch (Exception e) {
			e.printStackTrace();
			response.put("message", e.getMessage());
			return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		
	}
	
	@RequestMapping(value = "/medicinelist", method = RequestMethod.GET)
	public ResponseEntity<String> getMedicineList() throws Exception {
		try {
			List<UBSMedicines> medicinesList = Context.getService(UBSPrescriptionService.class).getMedicineList();
			String medicineJson = gson.toJson(medicinesList);
			return new ResponseEntity<>(medicineJson, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
