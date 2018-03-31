package com.testes;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.CompletionAwareAggregationStrategy;
import org.apache.camel.processor.aggregate.TimeoutAwareAggregationStrategy;

public class Agregacao implements AggregationStrategy, TimeoutAwareAggregationStrategy, CompletionAwareAggregationStrategy {

	@Override
	public void onCompletion(Exchange exchange) {
		
		System.out.println("=======> aconteceu onCompletion");
		
	}

	@Override
	public void timeout(Exchange oldExchange, int index, int total, long timeout) {
			
		System.out.println("=======> aconteceu timeout");
		
	}

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		
		if (oldExchange == null){
			
			String ret = (String) newExchange.getIn().getHeader("retorno");
			if (ret != null && ret.equals("servico_02")){
				newExchange.setProperty(Exchange.AGGREGATION_COMPLETE_CURRENT_GROUP, true);	
			}
			
			return newExchange;		
		}
		
		
		//controle para saber se houve exceção
		//importante sempre retornar oldExchange, se existir, pois é o ID dele que fica no banco como chave primária
		if (newExchange.getIn().getHeader("exception") !=null )
			oldExchange.getIn().setHeader("exception", "true");			

		return oldExchange;
	}

}
