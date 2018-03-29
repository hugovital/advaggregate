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
		
		if (oldExchange == null)
			return newExchange;
		
		if (newExchange.getIn().getHeader("exception") !=null )
			oldExchange.getIn().setHeader("exception", "true");

		return oldExchange;
	}

}
