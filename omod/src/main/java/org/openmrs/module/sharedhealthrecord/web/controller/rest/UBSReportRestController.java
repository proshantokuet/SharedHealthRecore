package org.openmrs.module.sharedhealthrecord.web.controller.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/rest/v1/ubs-report")
@RestController
public class UBSReportRestController {

	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getSelectedReport", method = RequestMethod.GET)
	public ResponseEntity<String> getSeletedReportByType(HttpServletRequest request) throws Exception {
		try {

				return new ResponseEntity<>("", HttpStatus.OK);

		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
}
