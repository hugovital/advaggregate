package com.testes;

import java.util.Arrays;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hawtdb.HawtDBAggregationRepository;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.OptimisticLockRetryPolicy;

public class MyRoutes extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		//HawtDBAggregationRepository hawtDBRepo = new HawtDBAggregationRepository("repo1", "c:/users/hugo/desktop/hawtdb.dat");		

		from("stream:in")
		
			.setHeader("id", constant(01))
		
			.log("${body}")
			
			.to("direct:serv_01")

			.log("${body}")
			
			.enrich("direct:serv_02", new AggregationStrategy() {

				@Override
				public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

					String[] str = new String[] {
						oldExchange.getIn().getBody(String.class),
						newExchange.getIn().getBody(String.class)
					};
					
					newExchange.getIn().setBody(Arrays.toString(str));

					return newExchange;
				}
			})
			.log("${body}");
		
		//servico 1
		from("direct:serv_01")
			.log("inside serv 01")
			.transform().constant("retorno do servico 01")
			.to("direct:aggregate");
		
		//servico 1
		from("direct:serv_02")
			.log("inside serv 02")
			.transform().constant("retorno do servico 02")
			//.throwException(new Exception("hugo servico 2 exception"))
			.to("direct:aggregate");

		HawtDBAggregationRepository hawtDBRepo = new HawtDBAggregationRepository("repo1", "c:/users/hugo/desktop/hawtdb.dat");
		OptimisticLockRetryPolicy hugoRetry = new OptimisticLockRetryPolicy();
		hugoRetry.setRetryDelay(10 * 1000); // <- 1 sec para ficar retentando
		hugoRetry.setExponentialBackOff(true);
		hugoRetry.setMaximumRetries(1);
		
		from("direct:aggregate")
			.aggregate( header("id") , new Agregacao() )
			//.aggregationRepository( hawtDBRepo )

				//aqui configurações para retry quando dá erro depois que completa o aggregate (isto é, dá erro no desfazimento)
				//.optimisticLocking()
				//.optimisticLockRetryPolicy( hugoRetry )

			.completionSize(2)
			.completionTimeout(10000)
			
			.to("direct:end_aggregate");
		
		from("direct:end_aggregate")

			.log("agregacaoComSucesso: ${property.agregacaoComSucesso}")
			.log("Final da Agregação: ${body}");
			
		/*
			.choice()

				.when ( simple("${property.agregacaoComSucesso} == true") )
					.log("Final da Agregação: ${body}")
				
				.when ( simple("${property.agregacaoDeuTimeout} == true") )				
					//aqui é o caso de erro na agregação (algum serviço não respondeu, deu timeout, etc)
					//daí abaixo seria um "to" para o serviço de desfazimento
					//mas como eu quero forçar o retry (caso onde falha o serviço de desfazimento), tome-lhe exception				
					//.throwException(new Exception("Hugo Exception"))
					
				.otherwise()
					//aqui é quando bem completou nem deu timeout, ou seja, quando retanta. vou dar exceção para ficar em loop

					//.throwException(new Exception("Hugo Exception"))
					
					
			.endChoice();
	*/
		

		from("direct:desfazer")
			.log("dentro do desfazimento");


	}
	
	

}
