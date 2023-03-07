package com.br.cucumberJava.steps;

import static com.br.cucumberJava.constants.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.br.cucumberJava.model.ModeloRequest;
import com.br.cucumberJava.utils.FileUtils;
import com.br.cucumberJava.utils.JsonUtils;
import com.br.cucumberJava.utils.RequestUtils;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ConsultaCepSteps {
	
	private final Logger LOGGER = LogManager.getLogger(this.getClass());
	private String cenario;
	private JSONArray array;
	private Response responseLocation;
	private ModeloRequest modeloRequest;


	@Before
	public void inicializar(Scenario scenario) throws Exception {
		this.cenario = scenario.getName();
		String json = FileUtils.getText("massa/ct001_consultar_cep_sucesso");
		JSONObject jsonData = new JSONObject(json);
		array = jsonData.getJSONArray("data");
	}
	

	@Given("^Consultar cep com a \"([^\"]*)\".$")
	public void given(String tipo, String massa) throws Exception {
		
		this.cenario = this.cenario.concat(": ").concat(massa);
		LOGGER.info("Iniciando o cenario {} ", this.cenario);
		
		JSONObject json = JsonUtils.getMassa(array, massa);
		if (json == null) {
			throw new Exception();
		}
		modeloRequest = new ModeloRequest();
		modeloRequest.setMethod(json.getString("method"));
		modeloRequest.setEndpoint(json.getString("endpoint"));
		modeloRequest.setHost(HOST_CRIAR_CONSULTAR_CEP);
		modeloRequest.setHeader(json.getJSONObject("header"));
		modeloRequest.setBody(json.getJSONObject("body").toString());
		
		if(!json.isNull("bodyResponse")) {			
			String bodyEsperado = json.getString("bodyResponse");			
			byte[] germanBytes = bodyEsperado.getBytes();					
			modeloRequest.setBodyResponse(new String(germanBytes, StandardCharsets.UTF_8));
		}
		
		if (modeloRequest.baseUrl() == null) {
			throw new Exception();
		}
		
	}

	@When("^Informo na \"([^\"]*)\".$")
	public void when(String requisicao, String tipo) throws Throwable {
		LOGGER.info("Processando requisição para consultar CEP {} ", this.cenario);
		responseLocation = RequestUtils.requestServico(modeloRequest.baseUrl(), modeloRequest.getMethod(),
				modeloRequest.getBody(), modeloRequest.getHeader().toMap());
		
	}

	@Then("^A requisição deve retornar (\\d+).$")
	public void then(int status) throws Throwable {
		
		try {
			if (status == responseLocation.getStatusCode()) {				
				assertEquals(status, responseLocation.getStatusCode());
			}else {				
				throw new AssertionError();				
			}
			
		} catch (AssertionError ass) {
			LOGGER.error(ass.toString());
			fail();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			fail();
		}
	}
}
