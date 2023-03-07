package com.br.cucumberJava.model;

import org.json.JSONObject;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ModeloRequest {

	private String method;
	private String endpoint;
	private String host;
	private JSONObject header;
	private String body;
	private String nameParams;
	private String params;
	private String response;
	private String bodyResponse;
	
	public String baseUrl() {
		if(this.host != null && !this.host.isEmpty() && this.endpoint != null && !this.endpoint.isEmpty()) {
			return this.host.concat(endpoint);
		}
		return null;
	}
	
}

