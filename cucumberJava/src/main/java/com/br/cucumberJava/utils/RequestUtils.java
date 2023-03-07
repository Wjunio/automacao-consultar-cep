package com.br.cucumberJava.utils;

import static io.restassured.RestAssured.given;

import java.sql.Timestamp;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import io.restassured.response.Response;

public class RequestUtils {

	private static final Logger log = LogManager.getLogger(RequestUtils.class);

    private RequestUtils() {
        throw new IllegalStateException("Utility class");
      }
	
	public static String getXrequestId() {

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long line = timestamp.getTime();
		String radix = Long.toString(line, 16);

		return StringUtils.leftPad(radix, 16, "0");
	}

	public static Response requestServico(String baseURI, String method, String body, Map<String, Object> headers)
			throws Exception {

		prettyPrintHeader(headers);
		prettyPrintJSON(body);

		Response response = null;

		if (method.equalsIgnoreCase("post")) {
			response = given().relaxedHTTPSValidation().headers(headers).body(body).post(baseURI);
		} else if (method.equalsIgnoreCase("get")) {
			response = given().relaxedHTTPSValidation().headers(headers).get(baseURI);
		}else if (method.equalsIgnoreCase("put")) {
			response = given().relaxedHTTPSValidation().headers(headers).body(body).put(baseURI);
		}else if (method.equalsIgnoreCase("patch")) {
			response = given().relaxedHTTPSValidation().headers(headers).body(body).patch(baseURI);
		}else if (method.equalsIgnoreCase("delete")) {
			response = given().relaxedHTTPSValidation().headers(headers).body(body).delete(baseURI);
		}
		
		return response;
	}

	public static Response requestServicoComParams(String baseURI, String method, String body,
        Map<String, Object> headers, Map<String, Object> params) throws Exception {

		prettyPrintHeader(headers);
		prettyPrintJSON(body);

		Response response = null;

		if (method.equalsIgnoreCase("get")) {
			response = given().relaxedHTTPSValidation().headers(headers).queryParams(params).get(baseURI);
		}

		return response;
	}

	private static void prettyPrintHeader(Map<String, Object> headers) {

		String print = headers
            .entrySet()
            .stream()
            .map( h -> "\t" + h.getKey() + "=" + h.getValue())
            .collect(Collectors.joining("\n"));
            log.debug(print);
	}
	
	private static void prettyPrintJSON(String jsonString) {

		if(jsonString != null) {
			JSONObject jo = new JSONObject(jsonString);
			String string = jo.toString(4);
			log.debug(string);
		}
	}
}
