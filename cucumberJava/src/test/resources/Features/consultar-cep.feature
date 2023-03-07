Feature: CONSULTAR CEP
	Scenario Outline: Consultar CEP e retorna o valor do frete.
		Given Consultar cep com a <massa>. 
		When Informo na <requisicao>.
		Then  A requisição deve retornar <status>.

	Examples:
		|massa|requisicao|status|
		|"ct001_consultar_cep_sucesso"|"body"|200|