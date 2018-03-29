package com.testes;

import java.util.Arrays;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class AgregacaoEnrichServ1Serv2 implements AggregationStrategy {

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		
		String[] str = new String[] {
				oldExchange.getIn().getBody(String.class),
				newExchange.getIn().getBody(String.class)
			};
			
		oldExchange.getIn().setBody(Arrays.toString(str));

		return oldExchange;

	}

}
