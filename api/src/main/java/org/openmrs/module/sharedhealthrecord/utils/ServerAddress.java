package org.openmrs.module.sharedhealthrecord.utils;

public class ServerAddress {
		
	public static String localServer(){
		
		//return "https://192.168.19.145/";
		return "https://localhost/";
	}
	
	public static String centralServer(){
		return "https://182.160.99.132/";
		//return "https://192.168.19.158/";
		
	}
	//0 for false 1 for true
	public static String isDeployInGlobal = "1";
	
	//url for downloading patient from local to server. no need to add slash / after the url
	public static String globalServerUrl = "https://182.160.99.132";
	
	//data whether sync to dhis2 or not
	public static int sendToDhisFromGlobal = 0;
		
}
