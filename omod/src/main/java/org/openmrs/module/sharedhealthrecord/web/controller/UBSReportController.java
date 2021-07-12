package org.openmrs.module.sharedhealthrecord.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UBSReportController {

	@RequestMapping(value = "/module/sharedhealthrecord/reports", method = RequestMethod.GET)
	public void globalServerSyncInfo(HttpServletRequest request, HttpSession session, Model model) {

		model.addAttribute("user", Context.getAuthenticatedUser());
			
	}
}
