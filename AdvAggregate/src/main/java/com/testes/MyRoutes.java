package com.testes;

import java.util.Arrays;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hawtdb.HawtDBAggregationRepository;
import org.apache.camel.processor.aggregate.AggregationStrategy;

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
			.to("direct:aggregate");


		HawtDBAggregationRepository hawtDBRepo = new HawtDBAggregationRepository("repo1", "c:/users/hugo/desktop/hawtdb.dat");			
		
		from("direct:aggregate")
			.aggregate( header("id") , new Agregacao() )
			.aggregationRepository( hawtDBRepo )
			.completionSize(2)
			.completionTimeout(10000)
			.to("direct:end_aggregate");
		
		from("direct:end_aggregate")
			.log("Final da Agregação: ${body}");

		from("direct:desfazer")
			.log("dentro do desfazimento");


	}
	
	

}
