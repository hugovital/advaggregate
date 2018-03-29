package com.testes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hawtdb.HawtDBAggregationRepository;
import org.apache.camel.processor.aggregate.OptimisticLockRetryPolicy;

public class MyRoutes extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		from("stream:in")
			
			.choice()
			
				.when( simple("${body} == 0" ) )
					.to("direct:sucesso") //chama serv01 e serv02, passa pelo aggregate que não termina por timeout, mas simple pelo completionSize(2)
				
				.when( simple("${body} == 1" ) )
					.to("direct:erro_serv_01")  //chama o serv01 com erro. não passa pelo aggregate já que estoura erro logo no serviço 1
				
				.when( simple("${body} == 2" ) )
					.to("direct:erro_serv_02")  //chama o serv01 com sucesso. dá erro no 02. o aggregate cai por timeout, e chama o desfazimento com sucesso

				.when( simple("${body} == 3" ) )
					.to("direct:erro_serv_02_compensate_error") //chama o serv01 com sucesso. dá erro no 02. o aggregate cai por timeout, e chama o desfazimento que dá erro. 
																//fica tentando várias vezes (até 10, configuradas). Se terminar o serviço java e subir de novo, continua tentando.
					                                            //btw, embora fique retentando quando sobre novamente, não para o processo. o serviço rest RestRoute nesse projeto
																//funciona normalmente
				.otherwise()
					.to("direct:texto")
					
			.endChoice();
		
		from("direct:sucesso")
			.setHeader("id", constant(01))
			.to("direct:serv_01_sucesso")
			.enrich("direct:serv_02", new AgregacaoEnrichServ1Serv2() )
			.log("${body}");
		
		from("direct:erro_serv_01")
			.setHeader("id", constant(01))
			.to("direct:serv_01_erro")
			.enrich("direct:serv_02", new AgregacaoEnrichServ1Serv2() )
			.log("${body}");

		from("direct:erro_serv_02")
			.setHeader("servicoCompensacao", constant("direct:serv_compensacao_sucesso"))
			.setHeader("id", constant(01))
			.to("direct:serv_01_sucesso")
			
			//o try ... catch abaixo é para mostrar um exemplo onde não queremos aguardar o timeout em caso de erros
			//dessa forma, capturamos a exceção, mandamos pro aggregate marcando que o fluxo está com erro
			//na hora de chamar o desfazimento, deixaremos chamar o serviço de compensação
			.doTry()
				.enrich("direct:serv_02_erro", new AgregacaoEnrichServ1Serv2() )
				.log("${body}")
			.doCatch(Exception.class)
				.setHeader("exception", constant("true"))
				.to("direct:aggregate")
			.endDoTry();
		
		from("direct:erro_serv_02_compensate_error")
			.setHeader("servicoCompensacao", constant("direct:serv_compensacao_erro"))
			.setHeader("id", constant(01))
			.to("direct:serv_01_sucesso")
			.enrich("direct:serv_02_erro", new AgregacaoEnrichServ1Serv2() )
			.log("${body}");
		
		from("direct:texto")
			.log("Digite:")
			.log("0- Sucesso")
			.log("1- Erro no serviço 1")
			.log("2- Erro no serviço 2")
			.log("3- Erro no serviço de compensação")
			.log("?- este tutorial");

		//servico 1 com sucesso
		from("direct:serv_01_sucesso")
			.log("inside serv 01")
			.transform().constant("retorno do servico 01")
			.to("direct:aggregate");
		
		//servico 2 com sucesso
		from("direct:serv_02")
			.log("inside serv 02")
			.transform().constant("retorno do servico 02")
			.to("direct:aggregate");
		
		//servico 1 com erro
		from("direct:serv_01_erro")
		    .throwException(new Exception("erro no servico 1 exception"))		
			.to("direct:aggregate");
		
		//servico 2 com sucesso
		from("direct:serv_02_erro")
			.log("inside serv 02")
			.log("${header.servicoCompensacao}")
			.transform().constant("retorno do servico 02")
			.throwException(new Exception("hugo servico 2 exception"))
			.to("direct:aggregate");

		HawtDBAggregationRepository hawtDBRepo = new HawtDBAggregationRepository("repo1", "c:/users/hugo/desktop/hawtdb.dat");
		OptimisticLockRetryPolicy hugoRetry = new OptimisticLockRetryPolicy();
		hugoRetry.setRetryDelay(10 * 1000); // <- 1 sec para ficar retentando
		hugoRetry.setExponentialBackOff(true);
		hugoRetry.setMaximumRetries(1);
		
		from("direct:aggregate")
			.aggregate( header("id") , new Agregacao() )
			.aggregationRepository( hawtDBRepo )

				//aqui configurações para retry quando dá erro depois que completa o aggregate (isto é, dá erro no desfazimento)
				.optimisticLocking()
				.optimisticLockRetryPolicy( hugoRetry )

			.completionSize(2)
			.completionTimeout(10000)
			
			.to("direct:end_aggregate");
		
		from("direct:end_aggregate")		
			.log(">>>após aggregate.")

			//o choice abaixo é imporante para deixar chamar a compensação só em casos de timeout ou de exceção que marcamos no header
			//lembrar-se que o fluxo normal (com sucesso) também irá passar por aqui, daí ser importante tratar para não chamar o desfazimento quando não precisar
			//essa é só uma das possíveis formas de fazer
			
			.choice()

				.when( simple("${header.CamelAggregatedCompletedBy} == 'timeout'") )
					.log("Roteando para: ${header.servicoCompensacao}")
					.toD("${header.servicoCompensacao}")
					
				.when ( simple("${header.exception}") )
					.log("Roteando para: ${header.servicoCompensacao}")
					.toD("${header.servicoCompensacao}")
					
				.otherwise()
					//doNothing, foi sucesso
					
			.endChoice();
					
		
		//servico compensacao com sucesso
		from("direct:serv_compensacao_sucesso")
			.transform().constant("serviço de compensação realizado com sucesso")
			.log("${body}");

		//servico compensacao com erro
		from("direct:serv_compensacao_erro")
			.throwException(new Exception("serviço de desfazimento com erro"))
			.log("${body}");


	}
	
	

}
