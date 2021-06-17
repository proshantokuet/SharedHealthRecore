package org.openmrs.module.sharedhealthrecord.web.controller.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.openmrs.module.sharedhealthrecord.dto.UBSPrescribedMedicinesDTO;
import org.openmrs.module.sharedhealthrecord.dto.UBSPrescriptionDTO;
import org.openmrs.module.sharedhealthrecord.utils.HeaderFooterPageEventPrescription;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@RequestMapping("/rest/v1/prescription")
@RestController
public class UBSPrescriptionRestController {
	
	Font titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, BaseColor.BLACK);
	
	Font textFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
	
	Font conditionFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, BaseColor.BLACK);
	
	Font paraFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 11, BaseColor.BLACK);
	
	Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, BaseColor.BLACK);
	
	Gson gson = new Gson();
	//public static DateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
	protected final Log log = LogFactory.getLog(this.getClass());

	@RequestMapping(value = "/save-update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> savePrescription(@RequestBody UBSPrescriptionDTO dto) throws Exception {
		
		JSONObject response = new JSONObject();
		log.error("DTO" + dto);
		try {
			
			Set<UBSPrescribedMedicinesDTO> UbsPrescribedMedicines = dto.getPrescribedMedicine();
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
			for (UBSPrescribedMedicinesDTO ubsMedicine : UbsPrescribedMedicines) {
				
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
				prescribedMedicine.setMedicineId(0);
				prescribedMedicine.setFrequency(ubsMedicine.getFrequency());
				prescribedMedicine.setDuration(ubsMedicine.getDuration());
				prescribedMedicine.setInstruction(ubsMedicine.getInstruction());
				ubsPrescribedMedicinesNew.add(prescribedMedicine);
			}

			ubsPrescription.setPrescribedMedicine(ubsPrescribedMedicinesNew);
			log.error("ubsprescrion Object Creating Seuccess full " + ubsPrescription.getPrescribedMedicine().size());
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
	public ResponseEntity<String> getMedicineList(@RequestParam(required = true) String type) throws Exception {
		try {
			List<UBSMedicines> medicinesList = Context.getService(UBSPrescriptionService.class).getMedicineList(type);
			String medicineJson = gson.toJson(medicinesList);
			return new ResponseEntity<>(medicineJson, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	

    @RequestMapping(value = "/prescriptionPdfGenerate",method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<byte[]> generatePdf(@RequestBody UBSPrescriptionDTO dto) throws IOException {
    	
    	// Firstly Saving the Prescription Info
    	
		Set<UBSPrescribedMedicinesDTO> UbsPrescribedMedicines = dto.getPrescribedMedicine();
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
		for (UBSPrescribedMedicinesDTO ubsMedicine : UbsPrescribedMedicines) {
			
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
			prescribedMedicine.setMedicineId(0);
			prescribedMedicine.setFrequency(ubsMedicine.getFrequency());
			prescribedMedicine.setDuration(ubsMedicine.getDuration());
			prescribedMedicine.setInstruction(ubsMedicine.getInstruction());
			ubsPrescribedMedicinesNew.add(prescribedMedicine);
		}

		ubsPrescription.setPrescribedMedicine(ubsPrescribedMedicinesNew);
		log.error("ubsprescrion Object Creating Seuccess full " + ubsPrescription.getPrescribedMedicine().size());
		UBSPrescription afterSavePrescription =  Context.getService(UBSPrescriptionService.class).saveorUpdate(ubsPrescription);
    	
    	
    	//Secondly creating the pdf
		
        byte[] pdf = createPdf(afterSavePrescription);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "pdf"));
        headers.setContentDispositionFormData("attachment", "x.pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<byte[]>(pdf, headers, HttpStatus.OK);
    }
    
    
	private byte[] createPdf(UBSPrescription dto) {
		//Document document = new Document();
        Document document = new Document(PageSize.A4, 36, 36, 36, 72);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
		                
	        PdfWriter writer = PdfWriter.getInstance(document, out);
	        HeaderFooterPageEventPrescription event = new HeaderFooterPageEventPrescription(dto);
			writer.setPageEvent(event);
			document.open();

			PdfPTable patientInformtionTable = new PdfPTable(2);
			patientInformtionTable.setWidthPercentage(100);
			addPatientInfo(patientInformtionTable,dto);
			document.add(patientInformtionTable);
			
	        float[] columnWidths = {3,6};

			PdfPTable medicationTable = new PdfPTable(columnWidths);
			medicationTable.setWidthPercentage(100);

			addMedicineInfoRow(medicationTable,dto);
			document.add(medicationTable);
			
		}
		catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			
			document.close();
		}
		 return out.toByteArray();
	}
	
    @RequestMapping(value = "/prescriptionDownload",method = RequestMethod.GET)
    public ResponseEntity<byte[]> generatePdfByvisitUuid(@RequestParam(required = true) String visituuid) throws IOException {

		UBSPrescription ubsPrescription = Context.getService(UBSPrescriptionService.class).findPrescriptionByVisitId(visituuid);
    	
    	//creating the pdf
		if(ubsPrescription != null) {
	        byte[] pdf = createPdf(ubsPrescription);
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(new MediaType("application", "pdf"));
	        headers.setContentDispositionFormData("attachment", "x.pdf");
	        headers.setContentLength(pdf.length);
	        return new ResponseEntity<byte[]>(pdf, headers, HttpStatus.OK);
		}
		else {
			byte[] pdf =  new byte[100];
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(new MediaType("application", "pdf"));
	        headers.setContentDispositionFormData("attachment", "x.pdf");
	        headers.setContentLength(pdf.length);
			return new ResponseEntity<byte[]>(pdf, headers, HttpStatus.NO_CONTENT);
		}
    }
	
	private void addPatientInfo(PdfPTable table, UBSPrescription dto) {
		
		String clinicName = "Patient Name: ";
		
		PdfPCell patientCell = new PdfPCell(new Paragraph(new Phrase(clinicName, paraFont)));
		//patientCell.setExtraParagraphSpace(2);
		patientCell.disableBorderSide(Rectangle.LEFT);
		patientCell.disableBorderSide(Rectangle.RIGHT);
		patientCell.disableBorderSide(Rectangle.BOTTOM);
		patientCell.disableBorderSide(Rectangle.TOP);

		//patientCell.setColspan(15);
		patientCell.setLeading(3, 1);
		table.addCell(patientCell);
		
		String clinicNameAnswer = dto.getPatientName();
		
		PdfPCell patientCellAnswer = new PdfPCell(new Paragraph(new Phrase(clinicNameAnswer, textFont)));
		//patientCellAnswer.setExtraParagraphSpace(2);
		patientCellAnswer.disableBorderSide(Rectangle.LEFT);
		patientCellAnswer.disableBorderSide(Rectangle.RIGHT);
		patientCellAnswer.disableBorderSide(Rectangle.BOTTOM);
		patientCellAnswer.disableBorderSide(Rectangle.TOP);		//patientCellAnswer.setColspan(15);
		//patientCellAnswer.disableBorderSide(Rectangle.BOTTOM);
		patientCellAnswer.setLeading(3, 1);
		table.addCell(patientCellAnswer);
		
		String clinicId = "Age: ";
		
		PdfPCell clinicIdCell = new PdfPCell(new Paragraph(new Phrase(clinicId, paraFont)));
		//clinicIdCell.setExtraParagraphSpace(2);
		clinicIdCell.setBorder(Rectangle.NO_BORDER);
		//clinicIdCell.disableBorderSide(Rectangle.BOTTOM);
		clinicIdCell.setLeading(3, 1);
		//clinicIdCell.setColspan(10);
		table.addCell(clinicIdCell);
		
		String clinicIdAnswer = dto.getPatientAge();
		
		PdfPCell clinicIdCellAnswer = new PdfPCell(new Paragraph(new Phrase(clinicIdAnswer, textFont)));
		//clinicIdCellAnswer.setExtraParagraphSpace(2);
		clinicIdCellAnswer.setBorder(Rectangle.NO_BORDER);
		//clinicIdCellAnswer.disableBorderSide(Rectangle.BOTTOM);
		clinicIdCellAnswer.setLeading(3, 1);
		//clinicIdCellAnswer.setColspan(5);
		table.addCell(clinicIdCellAnswer);
		
		
		String sateliteClinicId = "Sex: ";
		
		PdfPCell satelliteCLinicCell = new PdfPCell(new Paragraph(new Phrase(sateliteClinicId, paraFont)));
		//satelliteCLinicCell.setExtraParagraphSpace(2);
		satelliteCLinicCell.setBorder(Rectangle.NO_BORDER);
		//satelliteCLinicCell.disableBorderSide(Rectangle.BOTTOM);
		satelliteCLinicCell.setLeading(3, 1);
		//satelliteCLinicCell.setColspan(19);
		table.addCell(satelliteCLinicCell);
		
		String sateliteClinicIdAnswer = dto.getGender();
		
		PdfPCell sateliteClinicIdAnswerCell = new PdfPCell(new Paragraph(new Phrase(sateliteClinicIdAnswer, textFont)));
		//sateliteClinicIdAnswerCell.setExtraParagraphSpace(2);
		sateliteClinicIdAnswerCell.setBorder(Rectangle.NO_BORDER);
		//sateliteClinicIdAnswerCell.disableBorderSide(Rectangle.BOTTOM);
		sateliteClinicIdAnswerCell.setLeading(3, 1);
		//sateliteClinicIdAnswerCell.setColspan(5);
		table.addCell(sateliteClinicIdAnswerCell);
		
		String teamNo = "Appointment Date: ";
		
		PdfPCell teamNoCell = new PdfPCell(new Paragraph(new Phrase(teamNo, paraFont)));
		//teamNoCell.setExtraParagraphSpace(2);
		//teamNoCell.setBorder(Rectangle.NO_BORDER);
		teamNoCell.disableBorderSide(Rectangle.LEFT);
		teamNoCell.disableBorderSide(Rectangle.RIGHT);
		teamNoCell.disableBorderSide(Rectangle.TOP);	
		teamNoCell.disableBorderSide(Rectangle.BOTTOM);
		teamNoCell.setLeading(3, 1);
		//teamNoCell.setColspan(11);
		table.addCell(teamNoCell);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String teamNoAnswer = dateFormat.format(dto.getVisitDate());
		
		PdfPCell teamNoCellAnswer = new PdfPCell(new Paragraph(new Phrase(teamNoAnswer, textFont)));
		//teamNoCellAnswer.setExtraParagraphSpace(2);
		teamNoCellAnswer.disableBorderSide(Rectangle.LEFT);
		teamNoCellAnswer.disableBorderSide(Rectangle.RIGHT);
		teamNoCellAnswer.disableBorderSide(Rectangle.TOP);	
		teamNoCellAnswer.disableBorderSide(Rectangle.BOTTOM);
		teamNoCellAnswer.setLeading(3, 1);
		//teamNoCellAnswer.setColspan(8);
		table.addCell(teamNoCellAnswer);
		
		PdfPCell blankRow = new PdfPCell(new Phrase("\n"));
		blankRow.setFixedHeight(10f);
		blankRow.disableBorderSide(Rectangle.LEFT);
		blankRow.disableBorderSide(Rectangle.RIGHT);
		blankRow.disableBorderSide(Rectangle.TOP);		
		table.addCell(blankRow);
		
		PdfPCell blankRow2 = new PdfPCell(new Phrase("\n"));
		blankRow2.setFixedHeight(10f);
		blankRow2.disableBorderSide(Rectangle.LEFT);
		blankRow2.disableBorderSide(Rectangle.RIGHT);
		blankRow2.disableBorderSide(Rectangle.TOP);			
		table.addCell(blankRow2);
		
	}
	
	private void addMedicineInfoRow(PdfPTable medicationTable, UBSPrescription dto) {
		
		System.out.println("addMedicineInfoRow");
		String name = "";
		String frequency = "";
		String duration = "";
		String instruction = "";
		
		Paragraph historyPara = new Paragraph();
		historyPara.add(new Paragraph("Chief Complaints : "+ "\n" + "\n", paraFont));
		historyPara.add(new Paragraph(dto.getChiefComplaint()+ "\n" + "\n", textFont));
		historyPara.add(new Paragraph("Medical Diagnosis : "+ "\n"+ "\n", paraFont));
		historyPara.add(new Paragraph(dto.getDiagnosis()+ "\n" + "\n", textFont));
		historyPara.add(new Paragraph("Advice : " + "\n"+ "\n", paraFont));
		historyPara.add(new Paragraph(dto.getAdvice()+ "\n" + "\n", textFont));
		historyPara.add(new Paragraph(" "));
		

		PdfPCell historyCell = new PdfPCell();
		historyCell.addElement(historyPara);
		historyCell.setExtraParagraphSpace(2);
		historyCell.disableBorderSide(Rectangle.TOP);
		historyCell.disableBorderSide(Rectangle.LEFT);
		historyCell.disableBorderSide(Rectangle.RIGHT);
		historyCell.disableBorderSide(Rectangle.BOTTOM);
		historyCell.setLeading(4, 1);
		medicationTable.addCell(historyCell);
		
		int j = 1;		
		String prescriptions = "";
		for (UBSPrescribedMedicines prescribedMedicine : dto.getPrescribedMedicine()) {
			
			name = prescribedMedicine.getMedicineName();
			frequency = prescribedMedicine.getFrequency();
			duration = prescribedMedicine.getDuration() + " days";
			instruction = prescribedMedicine.getInstruction();
			prescriptions += j + ".  " + name + " - " + frequency + " - " + duration + " - " + instruction + "\n";
			j++;
		}
		
		Paragraph medicationPara = new Paragraph();
		medicationPara.add(new Paragraph("Medication : "+ "\n", paraFont));
		medicationPara.add(new Paragraph(prescriptions, textFont));

		
		//PdfPCell apponintmentCell = new PdfPCell(new Paragraph(new Phrase(prescriptions, textFont)));
		PdfPCell apponintmentCell = new PdfPCell();
		apponintmentCell.addElement(medicationPara);
		apponintmentCell.disableBorderSide(Rectangle.TOP);
		apponintmentCell.disableBorderSide(Rectangle.RIGHT);
		apponintmentCell.disableBorderSide(Rectangle.BOTTOM);
		apponintmentCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		//apponintmentCell.setExtraParagraphSpace(2);
		apponintmentCell.setLeading(4, 1);
		apponintmentCell.setBorderColorBottom(BaseColor.BLUE);
		medicationTable.addCell(apponintmentCell);
		

	}
	
}
