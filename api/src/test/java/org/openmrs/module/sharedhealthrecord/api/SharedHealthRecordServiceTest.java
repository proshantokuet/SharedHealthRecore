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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
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
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.sharedhealthrecord.domain.PersonAddress;
import org.openmrs.module.sharedhealthrecord.domain.PreferredName;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

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
		
		return requestBase;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void patient() {
		
		JSONParser jsonParser = new JSONParser();
		
		//try (FileReader reader = new FileReader("../patient.json")) {
		try {
			String patientUrlCentralServer = "https://192.168.33.10/openmrs/ws/rest/v1/patient";
			
			String patientUrl = "https://192.168.33.10/openmrs/ws/rest/v1/patient/48e7bfc3-d1d6-45bd-ac68-b024189ad5c6?v=full";
			String patientResponse = get(patientUrl, "", AuthType.BASIC, "superman:Admin123456");
			//Read JSON file
			JSONObject obj = (JSONObject) jsonParser.parse(patientResponse);
			
			String personUuid = (String) obj.get("uuid");
			String existingPatient = get(patientUrlCentralServer + "/" + personUuid, "v=full", AuthType.BASIC,
			    "superman:Admin123456");
			JSONObject getPatient = (JSONObject) jsonParser.parse(existingPatient);
			System.out.println("getPatient>>>>" + getPatient);
			String uuid = "";
			if (getPatient.containsKey("error")) {
				System.out.println("Not found");
			} else {
				
				uuid = "/" + personUuid;
			}
			System.out.println("uu..............." + uuid);
			String data = getPatientObject(obj, personUuid);
			System.out.println(data);
			String patientPostUrl = "https://192.168.33.10/openmrs/ws/rest/v1/bahmnicore/patientprofile";
			
			post(patientPostUrl + uuid, "", AuthType.BASIC, "superman:Admin123456", data);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getImage(String personUuid) {
		String patientUrl = "https://192.168.33.10/openmrs/ws/rest/v1/personimage/b6f2d814-d405-4661-a656-0c75a8f15480";
		String patientResponse = get(patientUrl, "", AuthType.BASIC, "superman:Admin123456");
		return "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";
	}
	
	@SuppressWarnings("unchecked")
	public static String getPatientObject(JSONObject obj, String personUuid) {
		
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject extractedPerson = (JSONObject) obj.get("person");
			JSONArray attributes = new JSONArray();
			JSONArray _attributes = new JSONArray();
			_attributes = (JSONArray) extractedPerson.get("attributes");
			attributes = generateAttrubutes(_attributes);
			JSONObject patient = new JSONObject();
			JSONObject personObject = new JSONObject();
			personObject.put("attributes", attributes);
			personObject.put("gender", extractedPerson.get("gender"));
			personObject.put("birthdate", extractedPerson.get("birthdate"));
			personObject.put("deathDate", extractedPerson.get("deathDate"));
			personObject.put("causeOfDeath", extractedPerson.get("causeOfDeath"));
			personObject.put("birthtime", extractedPerson.get("birthtime"));
			/********** name section *************/
			JSONArray names = new JSONArray();
			JSONObject preferredName = new JSONObject();
			preferredName = (JSONObject) extractedPerson.get("preferredName");
			PreferredName preName = new Gson().fromJson(preferredName.toString(), PreferredName.class);
			JSONObject name = (JSONObject) jsonParser.parse(new Gson().toJson(preName));
			names.add(name);
			personObject.put("names", names);
			
			JSONArray addresses = new JSONArray();
			
			JSONObject preferredAddress = new JSONObject();
			preferredAddress = (JSONObject) extractedPerson.get("preferredAddress");
			
			JSONObject _preferredAdress = (JSONObject) jsonParser.parse(new Gson().toJson(new Gson().fromJson(
			    preferredAddress.toString(), PersonAddress.class)));
			
			addresses.add(_preferredAdress);
			personObject.put("addresses", addresses);
			
			JSONObject personInfor = new JSONObject();
			personInfor.put("person", personObject);
			
			JSONArray _identifiers = new JSONArray();
			_identifiers = (JSONArray) obj.get("identifiers");
			
			personInfor.put("identifiers", getIdentifiers(_identifiers));
			patient.put("patient", personInfor);
			
			String url = "https://192.168.33.10/openmrs/ws/rest/v1/relationship";
			String relationshipResponse = get(url, "person=" + personUuid + "&v=full", AuthType.BASIC,
			    "superman:Admin123456");
			
			JSONObject _relationshipAsObject = (JSONObject) jsonParser.parse(relationshipResponse);
			JSONArray _relationshipArray = (JSONArray) _relationshipAsObject.get("results");
			
			patient.put("relationships", getRelationships(_relationshipArray));
			//patient.put("image", getImage(""));
			String data = patient.toJSONString();
			return data;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static JSONArray getIdentifiers(JSONArray _identifiers) {
		
		JSONArray identifiers = new JSONArray();
		_identifiers.forEach(idenf -> {
			JSONObject _identifier = (JSONObject) idenf;
			JSONObject identifier = new JSONObject();
			identifier.put("identifier", _identifier.get("identifier"));
			identifier.put("preferred", true);
			identifier.put("identifierType", "Patient_Identifier");
			identifiers.add(identifier);
		});
		return identifiers;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	public static JSONArray getRelationships(JSONArray _relationships) {
		JSONArray relationships = new JSONArray();
		_relationships.forEach(rela -> {
			JSONObject relationship = new JSONObject();
			JSONObject _relationshp = (JSONObject) rela;
			JSONObject relationShipType = (JSONObject) _relationshp.get("relationshipType");
			JSONObject personBObject = (JSONObject) _relationshp.get("personB");
			String relationshipUuid = (String) relationShipType.get("uuid");
			JSONObject relationshipTypeObject = new JSONObject();
			relationshipTypeObject.put("uuid", relationshipUuid);
			relationship.put("relationshipType", relationshipTypeObject);
			JSONObject personB = new JSONObject();
			personB.put("display", personBObject.get("display"));
			personB.put("uuid", personBObject.get("uuid"));
			relationship.put("personB", personB);
			
			relationships.add(relationship);
		});
		return relationships;
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
	
	public static String post(String url, String payload, AuthType authType, String authString, String data) {
		try {
			HttpPost request = (HttpPost) makeConnection(url, "", RequestMethod.POST, AuthType.BASIC, "superman:Admin123456");
			request.setHeader(HTTP.CONTENT_TYPE, "application/json");
			StringEntity entity = new StringEntity(data == null ? "" : data, "UTF-8");
			//System.err.println(data);
			entity.setContentEncoding("application/json");
			request.setEntity(entity);
			org.apache.http.HttpResponse response = httpClient.execute(request);
			String responseEntity = "";
			if (response.getEntity() != null) {
				responseEntity = IOUtils.toString(response.getEntity().getContent());
				System.out.println(responseEntity);
			}
			return responseEntity;
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static String get(String url, String payload, AuthType authType, String authString) {
		try {
			HttpGet request = (HttpGet) makeConnection(url, payload, RequestMethod.GET, authType, authString);
			org.apache.http.HttpResponse response = httpClient.execute(request);
			
			int statusCode = response.getStatusLine().getStatusCode();
			String entity = "";
			if (response.getEntity() != null) {
				entity = IOUtils.toString(response.getEntity().getContent());
			}
			return entity;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}
