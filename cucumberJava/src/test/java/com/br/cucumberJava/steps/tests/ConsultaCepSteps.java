package com.br.cucumberJava.steps.tests;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;


@RunWith(Cucumber.class)
@CucumberOptions(
		monochrome = true,
		features = "classpath:features/consultar-cep",
		glue = "com.br.cucumberJava.steps.tests"

)
public class ConsultaCepSteps {

}
