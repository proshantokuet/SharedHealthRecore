package org.openmrs.module.sharedhealthrecord.web.controller.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.api.UBSReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping("/rest/v1/ubs-report")
@RestController
public class UBSReportRestController {

	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getSelectedReport", method = RequestMethod.GET)
	public ResponseEntity<String> getSeletedReportByType(HttpServletRequest request) throws Exception {
		try {
				String startDate = request.getParameter("startDate");
				String endDate = request.getParameter("endDate");
				String reportName = request.getParameter("reportName");
				List<Object[]> selectedReport = Context.getService(UBSReportService.class).getSelectedReport(startDate, endDate, reportName);
		        ObjectMapper objectMapper = new ObjectMapper();
		        String value = objectMapper.writeValueAsString(selectedReport);
			    HttpHeaders headers = new HttpHeaders();
			    headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
				return ResponseEntity.ok().headers(headers).body(value);

		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
}
