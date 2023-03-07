package com.br.cucumberJava.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.restassured.http.Header;
import io.restassured.http.Headers;

public class JsonUtils {

	private static JSONObject returnRequestBody;
	private static JSONObject returnRequestHeader;
	private static Object valueFound;
	
	private static final Random rand = new Random();
	private static final Logger LOGGER = LogManager.getLogger(JsonUtils.class);

	public static JSONObject validJSON(String test) {
		try {
			return new JSONObject(test);
		} catch (JSONException ex) {
			LOGGER.warn("Objeto retornado não é uma JSON");
			return null;
		}
	}

	public static JSONObject convertJson(String value) {
		JSONObject jsonReturn = null;
		JSONArray jsArray = null;

		if (validJSON(value) != null) {
			jsonReturn = validJSON(value);
		} else {
			jsonReturn = new JSONObject();
			try {
				jsArray = new JSONArray(value);
				jsonReturn.put("return", jsArray);
			} catch (JSONException err) {
				
				jsonReturn.put("return", value);
			}
		}
		return jsonReturn;
	}

	public static JSONObject headerToJson(Headers headers) {
		if (headers.exist()) {
			JSONObject json = new JSONObject();
			for (Header header : headers) {
				json.put(header.getName(), header.getValue());
			}
			return json;
		}
		return null;
	}

	public static boolean checkKey(JSONObject object, String searchedKey) {
		boolean exists = object.has(searchedKey);
		if (!exists) {
			Set<String> keys = object.keySet();
			for (String key : keys) {
				if (object.get(key) instanceof JSONObject) {
					exists = checkKey((JSONObject) object.get(key), searchedKey);
				}
			}
		}
		return exists;
	}

	public static void changeParams(JSONObject objectMassa, JSONObject returnRequest) {
		if (returnRequest != null) {
			upgradeAllKeys(objectMassa, returnRequest);
		}
	}

	public static String changeEndpoint(String objectMassa, JSONObject returnRequest) {
		if (objectMassa.contains("{{") && objectMassa.contains("}}")) {
			String[] endpointText = objectMassa.split("/");
			for (int i = 0; i < endpointText.length; i++) {
				String textToReplace = endpointText[i].toString();
				if (textToReplace.startsWith("{{") && textToReplace.endsWith("}}")) {
					String findKey = textToReplace.substring(textToReplace.indexOf("{{") + 2,
							textToReplace.indexOf("}}"));
					upgradeValue(returnRequest, findKey);
					endpointText[i] = valueFound.toString();
				}
			}
			return String.join(",", endpointText).replace(",", "/");
		}
		return objectMassa;
	}

	public static JSONObject getMassa(JSONArray array, String massa) {
		for (int i = 0; i < array.length(); i++) {
			if (array.getJSONObject(i).get("massa").equals(massa)) {
				if (array.getJSONObject(i).has("params")
						&& (!array.getJSONObject(i).getJSONObject("params").isEmpty())) {
					changeParams(array.getJSONObject(i).getJSONObject("params"), returnRequestBody);
				}
				changeParams(array.getJSONObject(i).getJSONObject("body"), returnRequestBody);
				changeParams(array.getJSONObject(i).getJSONObject("header"), returnRequestHeader);
				if (array.getJSONObject(i).has("endpoint") && array.getJSONObject(i).has("method")
						&& array.getJSONObject(i).getString("method").toLowerCase().equals("get")) {
					array.getJSONObject(i).put("endpoint",
							changeEndpoint(array.getJSONObject(i).getString("endpoint"), returnRequestBody));
				}
				return array.getJSONObject(i);
			}
		}
		return null;
	}

	public static JSONObject getTargetDataBulk(JSONArray targetArray, String targetDataBulk) {

		JSONObject foundedDataBulkObject = IntStream
			.range(0, targetArray.length())
			.mapToObj( index -> targetArray.optJSONObject(index) )
			.filter( jo -> jo.getString("massa").equals(targetDataBulk) )
			.findFirst()
			.orElse(null)
		;

		if ( foundedDataBulkObject == null ) {
			return null;
		}

		else {

			JSONObject header = updateDataBulkValues(foundedDataBulkObject.optJSONObject("header"), returnRequestHeader);
			JSONObject body = updateDataBulkValues(foundedDataBulkObject.optJSONObject("body"), returnRequestBody);

			foundedDataBulkObject.put("header", header);
			foundedDataBulkObject.put("body", body);

			return  foundedDataBulkObject;
		}

	}

	public static JSONObject getDataBulkById(JSONArray targetArray, String idDataBulk) {

		return IntStream
				.range(0, targetArray.length())
				.mapToObj( index -> targetArray.getJSONObject(index))
				.filter( jo -> jo.optString("_id").equals(idDataBulk) )
				.findFirst()
				.orElse(new JSONObject())
		;
	}

	private static JSONObject updateDataBulkValues(JSONObject targetJson, JSONObject dataJson) {

		String regexText = "(\\{\\{)(.*)(\\}\\})";

		Pattern pattern = Pattern.compile(regexText);

		Map<String, Object> mapa = targetJson.toMap();

		for (String key : mapa.keySet()) {
			Matcher matcher = pattern.matcher(targetJson.optString(key));
			if (matcher.find()) {
				String match = matcher.group(2);
				targetJson.put(key, dataJson.opt(match));
			}
		}

		return new JSONObject(targetJson.toString());
	}

	public static void setMassaBody(JSONObject object) {
		if (returnRequestBody != null) {
			for (String key : object.keySet()) {
				returnRequestBody.put(key, object.get(key));
			}
		} else {
			returnRequestBody = object;
		}
		LOGGER.trace("Inserindo valores dos bodies");
	}

	public static void setMassaHeader(JSONObject object) {
		returnRequestHeader = object;
		LOGGER.trace("Inserindo valores dos headers");
	}

	public static JSONObject getReturnRequestBody() {
		return returnRequestBody;
	}

	public static JSONObject getReturnRequestHeader() {
		return returnRequestHeader;
	}

	private static void upgradeValue(JSONObject returnObject, String findKey) {
		for (String key : returnObject.keySet()) {
			Object obj = returnObject.get(key);

			if (obj instanceof JSONObject) {
				JSONObject modifiedJsonobject = (JSONObject) returnObject.get(key);
				if (modifiedJsonobject != null || valueFound == null) {
					upgradeValue(modifiedJsonobject, findKey);
				}
			} else {
				if (key.equals(findKey)) {
					valueFound = obj;
				}
			}

		}

	}

	private static void upgradeAllKeys(JSONObject jsonObject, JSONObject returnObject) {
		for (String key : jsonObject.keySet()) {
			Object obj = jsonObject.get(key);
			if (obj instanceof String) {
				String textToReplace = obj.toString();
				if (textToReplace.startsWith("{{") && textToReplace.endsWith("}}")) {
					String findKey = textToReplace.substring(textToReplace.indexOf("{{") + 2,
							textToReplace.indexOf("}}"));
					valueFound = "{{Coloque a key correta!}}";
					upgradeValue(returnObject, findKey);
					jsonObject.put(key, valueFound);
				}
			} else if (obj instanceof JSONObject) {
				JSONObject modifiedJsonobject = (JSONObject) jsonObject.get(key);
				if (modifiedJsonobject != null) {
					upgradeAllKeys(modifiedJsonobject, returnObject);
				}
			}

		}
	}

	public static String getJsonValue(JSONObject json, String key) {
		boolean exists = json.has(key);
		Iterator<?> keys;
		String nextKeys;
		String val = "";
		if (!exists) {
			keys = json.keys();
			while (keys.hasNext()) {
				nextKeys = (String) keys.next();
				try {
					if (json.get(nextKeys) instanceof JSONObject) {
						JSONObject jsonRecursive = (JSONObject) json.get(nextKeys);
						return getJsonValue(jsonRecursive, key);
					} else if (json.get(nextKeys) instanceof JSONArray) {
						JSONArray jsonArray = json.getJSONArray(nextKeys);
						int i = 0;
						if (i < jsonArray.length())
							do {
								String jsonArrayString = jsonArray.get(i).toString();
								JSONObject innerJson = new JSONObject(jsonArrayString);
								return getJsonValue(innerJson, key);
							} while (i < jsonArray.length());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			val = json.get(key).toString();
		}
		return val;
	}

	public static String juntaCpf(String cpf, String codCpf) {
		return cpf + codCpf;
	}

	public static Long getCpfcnpjfildig() {
		String cpfCnpj = returnRequestBody.optString("cpfCnpj");
		Integer codFilialCpfCnpj = returnRequestBody.optInt("codFilialCnpj");
		String codControleCpfCnpj = returnRequestBody.optString("codControleCpfCnpj");

		return Long.parseLong(cpfCnpj + String.format("%04d", codFilialCpfCnpj) + codControleCpfCnpj);
	}

	public static String getCpf11() {

		return returnRequestBody.optString("cpfCnpj") + returnRequestBody.optString("codControleCpfCnpj");
	}

	public static void setCpf11(String cpf11) {
		returnRequestBody.put("cpf11", cpf11);
	}
	
	public static Integer getAgencia() {
		return returnRequestBody.optInt("agencia");
	}

	public static Integer getConta() {
		return returnRequestBody.optInt("conta");
	}

	public static Integer getDac() {
		return returnRequestBody.optInt("dac");
	}

	public static void setStatelessOpen(String statelessOpen) {
		returnRequestHeader.put("x-stateless-open", statelessOpen);
	}

	public static String getStatelessOpen() {
		return returnRequestHeader.optString("x-stateless-open");
	}

	public static String getStatelessClosed() {
		return returnRequestHeader.optString("x-stateless-closed");
	}


	/**
	 *  setBodyValues acerta um valor arbitrário ao objeto
	 *  estático returnRequestBody.
	 *
	 *  Útil para transferir informações entre os casos
	 *  de teste.
	 *
	 * @param key
	 * @param value
	 */
	public static void setBodyValue(String key, Object value) {

		returnRequestBody.put(key, value);
		LOGGER.trace("setBodyValue: " + key + " -> " + value);
	}

	public static void setBodyValue(String key, String value) {

		returnRequestBody.put(key, value);
		LOGGER.trace("setBodyValue: " + key + " -> " + value);
	}

	public static void setBodyValue(String key, Integer value) {

		returnRequestBody.put(key, value);
		LOGGER.trace("setBodyValue: " + key + " -> " + value);
	}

	public static void setBodyValue(String key, JSONArray value) {

		returnRequestBody.put(key, value);
		LOGGER.trace("setBodyValue: " + key + " -> " + value.toString(4));
	}

	public static void setBodyValue(String key, JSONObject value) {

		returnRequestBody.put(key, value);
		LOGGER.trace("setBodyValue: " + key + " -> " + value.toString(4));
	}

	/**
	 *  setHeaderValues acerta um valor arbitrário ao objeto
	 *  estático returnRequestBody.
	 *
	 *  Útil para transferir informações entre os casos
	 *  de teste.
	 * @param key
	 * @param value
	 */
	public static void setHeaderValue(String key, Object value) {

		returnRequestHeader.put(key, value);
		LOGGER.trace("setHeaderValue: " + key + " -> " + value);
	}

	public static void setHeaderValue(String key, String value) {

		returnRequestHeader.put(key, value);
		LOGGER.trace("setHeaderValue: " + key + " -> " + value);
	}

	public static void setHeaderValue(String key, Integer value) {

		returnRequestHeader.put(key, value);
		LOGGER.trace("setHeaderValue: " + key + " -> " + value);
	}

	public static void setHeaderValue(String key, JSONArray value) {

		returnRequestHeader.put(key, value);
		LOGGER.trace("setHeaderValue: " + key + " -> " + value.toString(4));
	}

	public static void setHeaderValue(String key, JSONObject value) {

		returnRequestHeader.put(key, value);
		LOGGER.trace("setHeaderValue: " + key + " -> " + value.toString(4));
	}

	/**
	 * Alterna entre a enumeração de tipos de pessoa.
	 *
	 * Pessoa física: F -&gt; 1
	 * Pessoa jurídica: J -&gt; 2
	 *
	 * Comportamento padrão de pessoa física.
	 */
	public static void switchTipoPessoa() {

		String tipoCliente = returnRequestBody.getString("tipoCliente");

		switch (tipoCliente) {
			case "J":
				JsonUtils.setBodyValue("tipoPessoa", 2);
				break;
			case "F":
			default:
				JsonUtils.setBodyValue("tipoPessoa", 1);
		}
	}

	/**
	 * Mapeia um contrato bruto oriundo da API para o
	 * esquema JSON de um contrato selecionado (selected contract)
	 *
	 * @param contractObj
	 * @return
	 */
	public static JSONObject getSelectedContractEmpf(JSONObject contractObj) {
		JSONObject selectedContract = new JSONObject();

		selectedContract.put("numeroContrato", contractObj.getInt("numeroContratoEmpf"));
		selectedContract.put("numeroContratoProduto", contractObj.getInt("produtoEmpf"));
		selectedContract.put("numeroContratoFamilia", contractObj.getInt("fmlEmpf"));

		return selectedContract;
	}

	/**
	 * Mapeia um contrato bruto oriundo da API para o
	 * esquema JSON de um contrato selecionado (selected contract)
	 *
	 * @param contractObj
	 * @return
	 */
	public static JSONObject getSelectedContractLimite(JSONObject contractObj) {
		JSONObject selectedContract = new JSONObject();

		selectedContract.put("numeroContrato", contractObj.getInt("numeroContratoLimite"));
		selectedContract.put("numeroContratoProduto", contractObj.getInt("produtoContrato"));
		selectedContract.put("numeroContratoFamilia", contractObj.getInt("fmlContrato"));

		return selectedContract;
	}

	/**
	 * Soma os valores da chave "valorContratoLimite" dos contrados de Limite.
	 *
	 * @param contratosLimite
	 * @return
	 */

	public static double getTotalLimite(JSONArray contratosLimite) {
		return IntStream.range(0, contratosLimite.length())
				.mapToObj( index -> contratosLimite.optJSONObject(index) )
				.map( obj -> obj.optDouble("valorContratoLimite") )
				.reduce(0.0, Double::sum )
				;
	}

	/**
	 * Soma os valores da chave "valorContratoEmpf" dos contrados de Emprestimos.
	 * @param contratosEmprestimo
	 * @return
	 */
	public static double getTotalEmp(JSONArray contratosEmprestimo) {
		return IntStream.range(0, contratosEmprestimo.length())
				.mapToObj( index -> contratosEmprestimo.optJSONObject(index) )
				.map( obj -> obj.optDouble("valorContratoEmpf") )
				.reduce(0.0, Double::sum )
				;
	}

	public static JSONObject getRandomReogSimulation(JSONArray... jsonArrays) {

		JSONArray merged = new JSONArray();

		Arrays.stream(jsonArrays)
		.forEach( jsonArray -> {
			IntStream.range(0, jsonArray.length())
					.mapToObj(jsonArray::optJSONObject)
					.forEach(merged::put);
		});

		if (merged.isEmpty()) {
			return new JSONObject();
		}
		else {
			return merged.optJSONObject(rand.nextInt(merged.length()));
		}
	}
}
