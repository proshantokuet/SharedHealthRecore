/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.sharedhealthrecord.api;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;

import org.json.JSONException;
import org.junit.Test;
import org.openmrs.module.sharedhealthrecord.UBSPrescription;
import org.openmrs.module.sharedhealthrecord.dto.UBSPrescriptionDTO;
import org.openmrs.module.sharedhealthrecord.utils.HeaderFooterPageEventPrescription;
import org.openmrs.test.BaseModuleContextSensitiveTest;

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

/**
 * Tests {@link $ PSIService} .
 */
public class PrescriptionPDFTest extends BaseModuleContextSensitiveTest {
	
	Font titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, BaseColor.BLACK);
	
	Font textFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
	
	Font conditionFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, BaseColor.BLACK);
	
	Font paraFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 11, BaseColor.BLACK);
	
	Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, BaseColor.BLACK);
	
	@Test
	public void shouldSetupContext() throws ParseException, JSONException {

		//Document document = new Document();
        Document document = new Document(PageSize.A4, 36, 36, 36, 72);

		try {
			PdfWriter writer = null;
			try {
				writer = PdfWriter.getInstance(document, new FileOutputStream("/opt/bahmni/bahmni-emr/"
					        + "money receipt" + ".pdf"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			UBSPrescription dto = new UBSPrescription();
			HeaderFooterPageEventPrescription event = new HeaderFooterPageEventPrescription(dto);
			writer.setPageEvent(event);
			document.open();

			PdfPTable patientInformtionTable = new PdfPTable(2);
			patientInformtionTable.setWidthPercentage(100);
			addPatientInfo(patientInformtionTable);
			document.add(patientInformtionTable);
			
	        float[] columnWidths = {3,6};

			PdfPTable medicationTable = new PdfPTable(columnWidths);
			medicationTable.setWidthPercentage(100);

			addMedicineInfoRow(medicationTable);
			document.add(medicationTable);
			
		}
			catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			document.close();
		}
	}
	
	private void addPatientInfo(PdfPTable table) {
		
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
		
		String clinicNameAnswer = "Md.Tanvir Rahman";
		
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
		
		String clinicIdAnswer = "24 years 11 Month 3 Days";
		
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
		
		String sateliteClinicIdAnswer = "Male";
		
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
		
		String teamNoAnswer = "2021-06-08";
		
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
	
	private void addMedicineInfoRow(PdfPTable medicationTable) {
	
		System.out.println("addMedicineInfoRow");
		String name = "";
		String frequency = "";
		String duration = "";
		String instruction = "";
		Paragraph patientPara = new Paragraph();


		
		Paragraph historyPara = new Paragraph();
		historyPara.add(new Paragraph("Chief Complaints : "+ "\n" + "\n", paraFont));
		historyPara.add(new Paragraph("Joint Pain,Blurred Vision,Tender nech,Neck Mass,Chest Pain,Palpitations"+ "\n" + "\n", textFont));
		historyPara.add(new Paragraph("Medical Diagnosis : "+ "\n"+ "\n", paraFont));
		historyPara.add(new Paragraph("Lab Test,Hiv Test,Blood Test"+ "\n" + "\n", textFont));
		historyPara.add(new Paragraph("Advice : " + "\n"+ "\n", paraFont));
		historyPara.add(new Paragraph("Take Sleep and Rest"+ "\n" + "\n", textFont));
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
		//String prescriptions = "Medication : "+ "\n" + "\n";
		String prescriptions = "";
		name = "Napa-Extra";
		frequency = "Thice a week";
		duration = "7 Days";
		instruction = "Take Rest and drink enough water to make you hydrated";
		
		for(int i = 0; i< 15; i++) {
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
