package org.openmrs.module.sharedhealthrecord.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.sharedhealthrecord.UBSPrescription;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class HeaderFooterPageEventPrescription extends PdfPageEventHelper {
	protected final Log log = LogFactory.getLog(getClass());
	
	Font titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, BaseColor.BLACK);
	
	Font textFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, BaseColor.BLACK);
	
	Font textHeader = FontFactory.getFont(FontFactory.TIMES_BOLD, 15, BaseColor.BLACK);
	
	Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, BaseColor.BLACK);
	
	PdfTemplate total;
	String header;
	UBSPrescription receiptPdfDTO;
	
	public HeaderFooterPageEventPrescription(UBSPrescription dto) {
		
		this.receiptPdfDTO = dto;
	}
    public void setHeader(String header) {
        this.header = header;
    }
	@Override
	public void onOpenDocument(PdfWriter writer, Document document) {
           total = writer.getDirectContent().createTemplate(30, 16);
    }
	
	@Override
	public void onStartPage(PdfWriter writer, Document document) {
		//Path path = Paths.get("/opt/bahmni/bahmni-emr/openmrs-module-bahmniapps/ui/app/images/bahmniLogoFull.png");
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 14, BaseColor.BLACK);
		try {
			File file1 = new File("/opt/bahmni-web/etc/bahmniapps/images/bahmniLogoFull.png");
			String absolutePath1 = file1.getAbsolutePath();
			log.error("Image path first" + absolutePath1);
			
			Image img = Image.getInstance(absolutePath1);
			img.setAlignment(Image.ALIGN_LEFT);
			img.scalePercent(10);
			//document.add(img);
			
			PdfPTable h = new PdfPTable(2);
			h.setWidthPercentage(100);
			h.setSpacingAfter(1f);
			header(h, img,writer);
			document.add(h);
			
		}
		catch (DocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
		}
		
	}
	
	private void header(PdfPTable table, Image img,PdfWriter writer) {
		
		PdfPCell imageCell = new PdfPCell(img);
		imageCell.disableBorderSide(Rectangle.RIGHT);
		imageCell.disableBorderSide(Rectangle.TOP);
		imageCell.disableBorderSide(Rectangle.LEFT);
		imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(imageCell);
		
		PdfPCell apponintmentCell = new PdfPCell(new Paragraph(new Phrase(receiptPdfDTO.getProviderName(), textHeader)));
		apponintmentCell.disableBorderSide(Rectangle.RIGHT);
		apponintmentCell.disableBorderSide(Rectangle.TOP);
		apponintmentCell.disableBorderSide(Rectangle.LEFT);
		apponintmentCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		apponintmentCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		apponintmentCell.setNoWrap(true);
		apponintmentCell.setLeading(3, 1);
		table.addCell(apponintmentCell);

	}
	
	  public void onEndPage(PdfWriter writer, Document document) {
		  PdfPTable table = new PdfPTable(3);

              try {
				table.setWidths(new int[]{24, 24, 2});
			
              table.setTotalWidth(527);
              table.setLockedWidth(true);
              table.getDefaultCell().setFixedHeight(20);
              table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
              table.addCell(header);
              table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
              table.addCell(String.format("Page %d of", writer.getPageNumber()));
              System.out.println(String.format("Page %d", writer.getPageNumber()));
              PdfPCell cell = new PdfPCell(Image.getInstance(total));
              cell.setBorder(Rectangle.NO_BORDER);
              table.addCell(cell);
              table.writeSelectedRows(0, -1, 36, 64, writer.getDirectContent());
              
              } catch (DocumentException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
 
      }
	
    public void onCloseDocument(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                new Phrase(String.valueOf(writer.getPageNumber())),
                2, 2, 0);
    }
	

	

	
	private void totalNumberOfPages(PdfPTable table,PdfWriter writer) {
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(String.format("Page %d", writer.getPageNumber()));
        //table.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
	}
	
}
