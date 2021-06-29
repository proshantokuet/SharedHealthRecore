package org.openmrs.module.sharedhealthrecord.web.listener;

import com.jayway.jsonpath.Configuration;

public class UBSConfigurationJsonPath {

	public static Object parseDocument(String IntialJsonDHISArray) {
		return Configuration.defaultConfiguration().jsonProvider()
				.parse(IntialJsonDHISArray);
	}

}
