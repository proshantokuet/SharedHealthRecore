package org.openmrs.module.sharedhealthrecord.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.RequestMethod;

public class HttpUtil {
	
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
	
	public static String post(String url, String payload, String data) {
		try {
			HttpPost request = (HttpPost) makeConnection(url, "", RequestMethod.POST, AuthType.BASIC, "admin:test");
			request.setHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8");
			StringEntity entity = new StringEntity(data == null ? "" : data, "UTF-8");
			//System.err.println(data);
			entity.setContentEncoding("application/json; charset=UTF-8");
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
	
	public static String get(String url, String payload, String authString) {
	
		try {
			HttpGet request = (HttpGet) makeConnection(url, payload, RequestMethod.GET, AuthType.BASIC, authString);
			//request.setHeader("Content-Type", "application/json; charset=UTF-8");
			//request.setHeader("accept-charset", "UTF-8");
			//request.setHeader("Accept-Encoding", "UTF-8");
			org.apache.http.HttpResponse response = httpClient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			
			String entity = "";
			if (response.getEntity() != null) {
				//HttpEntity entity = response.getEntity();
				//entity = IOUtils.toString(response.getEntity().getContent());
				entity = EntityUtils.toString(response.getEntity(),"UTF-8");
			}
			return entity;

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	
	public static String delete(String url, String payload, String authString) {
		try {
			HttpDelete request = (HttpDelete) makeConnection(url, payload, RequestMethod.DELETE, AuthType.BASIC, authString);
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
