package org.openmrs.module.sharedhealthrecord.web.listener;

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
//	@Autowired
//	SHRActionAuditInfoService actionAuditInfoService;
//	@Autowired
//	SHRActionErrorLogService actionErrorLogService;
	
	@SuppressWarnings("rawtypes")
	public void sendData() throws Exception {
		
	}
	
}
