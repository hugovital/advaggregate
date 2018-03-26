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
		
		oldExchange.setProperty("agregacaoDeuTimeout", true); //<--- importante marcar o timeout para controle no fluxo pó-agregação		
			
		System.out.println("=======> aconteceu timeout");
		
	}

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		
		if (oldExchange == null)
			return newExchange;
		
		//se chegou aqui, é porque existe um oldExchange, o que quer dizer que duas mensagens foram recebidas.
		//como no meu caso são 2 serviços que têm que aconecer, isso é um caso de sucesso
		
		newExchange.setProperty("agregacaoComSucesso", true);
		
		return newExchange;
	}

}
