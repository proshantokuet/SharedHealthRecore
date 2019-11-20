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

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests {@link $ SharedHealthRecordService} .
 */
public class SharedHealthRecordServiceTest extends BaseModuleContextSensitiveTest {
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(SharedHealthRecordService.class));
	}
	
	private final static DefaultHttpClient httpClient = init();
	
	public static DefaultHttpClient init() {
		try {
			//TODO add option to ignore cetificate validation in opensrp.prop
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			CustomCertificateSSLSocketFactory sf = new CustomCertificateSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(CustomCertificateSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			
			BasicHttpParams basicHttpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(basicHttpParams, 30000);
			HttpConnectionParams.setSoTimeout(basicHttpParams, 60000);
			
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));
			
			ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(basicHttpParams, registry);
			return new DefaultHttpClient(connectionManager, basicHttpParams);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public enum AuthType {
		BASIC, TOKEN, NONE
	}
	
	static HttpRequestBase makeConnection(String url, String payload, RequestMethod method, AuthType authType,
	                                      String authString) throws URISyntaxException {
		String charset = "ISO 8859-1";
		
		if (url.endsWith("/")) {
			url = url.substring(0, url.lastIndexOf("/"));
		}
		url = (url + (payload.isEmpty() ? "" : ("?" + payload))).replaceAll(" ", "%20");
		URI urlo = new URI(url);
		
		HttpRequestBase requestBase = null;
		if (method.equals(RequestMethod.GET)) {
			requestBase = new HttpGet(urlo);
		} else if (method.equals(RequestMethod.POST)) {
			requestBase = new HttpPost(urlo);
		} else if (method.equals(RequestMethod.PUT)) {
			requestBase = new HttpPut(urlo);
		} else if (method.equals(RequestMethod.DELETE)) {
			requestBase = new HttpDelete(urlo);
		}
		requestBase.setURI(urlo);
		requestBase.addHeader("Accept-Charset", charset);
		//requestBase.addHeader("Accept-Language", "en-US,bn");
		
		if (authType.name().equalsIgnoreCase("basic")) {
			String encoded = authString.matches(".+:.+") ? new String(Base64.encodeBase64(authString.getBytes()))
			        : authString;
			requestBase.addHeader("Authorization", "Basic " + encoded);
		} else if (authType.name().equalsIgnoreCase("token")) {
			requestBase.addHeader("Authorization", "Token " + authString);
		}
		
		System.out.println(requestBase);
		return requestBase;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void patient() {
		
		JSONParser jsonParser = new JSONParser();
		
		try (FileReader reader = new FileReader("../patient.json")) {
			//Read JSON file
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
			JSONObject patient = new JSONObject();
			JSONObject personObject = new JSONObject();
			//JSONArray employeeList = (JSONArray) obj;
			//System.out.println(obj);
			JSONObject extractedPerson = (JSONObject) obj.get("person");
			JSONArray attributes = new JSONArray();
			JSONArray _attributes = new JSONArray();
			_attributes = (JSONArray) extractedPerson.get("attributes");
			//attributes = generateAttrubutes(_attributes);
			
			//System.err.println(attributes);
			
			personObject.put("attributes", attributes);
			personObject.put("gender", extractedPerson.get("gender"));
			personObject.put("birthdate", extractedPerson.get("birthdate"));
			personObject.put("deathDate", extractedPerson.get("deathDate"));
			personObject.put("causeOfDeath", extractedPerson.get("causeOfDeath"));
			personObject.put("birthtime", extractedPerson.get("birthtime"));
			
			JSONArray names = new JSONArray();
			JSONObject preferredName = new JSONObject();
			preferredName = (JSONObject) extractedPerson.get("preferredName");
			preferredName.remove("links");
			preferredName.remove("resourceVersion");
			preferredName.remove("voided");
			preferredName.remove("familyName2");
			preferredName.put("preferred", true);
			names.add(preferredName);
			personObject.put("names", names);
			
			JSONArray addresses = new JSONArray();
			personObject.put("addresses", addresses);
			/*JSONObject preferredAddress = new JSONObject();
			preferredAddress = (JSONObject) extractedPerson.get("preferredAddress");
			preferredAddress.remove("display");
			preferredAddress.remove("uuid");
			preferredAddress.remove("links");
			preferredAddress.remove("resourceVersion");
			addresses.add(preferredAddress);*/
			
			//personObject.put("addresses", addresses);
			
			JSONObject personInfor = new JSONObject();
			personInfor.put("person", personObject);
			
			JSONArray _identifiers = new JSONArray();
			JSONArray identifiers = new JSONArray();
			_identifiers = (JSONArray) obj.get("identifiers");
			_identifiers.forEach(idenf -> {
				JSONObject _identifier = (JSONObject) idenf;
				JSONObject identifier = new JSONObject();
				identifier.put("identifier", _identifier.get("identifier"));
				identifier.put("preferred", true);
				identifier.put("identifierType", "Patient_Identifier");
				identifiers.add(identifier);
			});
			personInfor.put("identifiers", identifiers);
			patient.put("patient", personInfor);
			JSONArray rel = new JSONArray();
			patient.put("relationships", rel);
			System.out.println(patient);
			String data = patient.toJSONString();
			
			try {
				HttpPost request = (HttpPost) makeConnection(
				    "https://192.168.33.10/openmrs/ws/rest/v1/bahmnicore/patientprofile", "", RequestMethod.POST,
				    AuthType.BASIC, "superman:Admin123456");
				request.setHeader(HTTP.CONTENT_TYPE, "application/json");
				StringEntity entity = new StringEntity(data == null ? "" : data, "UTF-8");
				
				entity.setContentEncoding("application/json");
				request.setEntity(entity);
				org.apache.http.HttpResponse response = httpClient.execute(request);
				System.out.println(response);
				
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	//https://192.168.19.146/openmrs/openmrs/ws/rest/v1/bahmnicore/patientprofile
	@SuppressWarnings("unchecked")
	private static JSONArray generateAttrubutes(JSONArray _attributes) {
		JSONArray attributes = new JSONArray();
		_attributes.forEach(attr -> {
			JSONObject _attribute = new JSONObject();
			JSONObject _atrrType = new JSONObject();
			_attribute = (JSONObject) attr;
			
			JSONObject attribute = new JSONObject();
			JSONObject attributeType = new JSONObject();
			_atrrType = (JSONObject) _attribute.get("attributeType");
			attributeType.put("uuid", _atrrType.get("uuid"));
			attributeType.put("name", _atrrType.get("display"));
			attribute.put("attributeType", attributeType);
			String valueAsString = (String) _attribute.get("display");
			System.err.println(valueAsString);
			if (valueAsString != null) {
				String[] values = valueAsString.split("=");
				String value = "";
				if (values.length == 1) {
					value = values[0];
				} else {
					value = values[1];
				}
				attribute.put("value", value);
			}
			attributes.add(attribute);
		});
		return attributes;
	}
	
}
