package com.testes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spi.AggregationRepository;

/* Classe de Teste de AggregationRepository Custom */
public class MyAggregationRepository implements AggregationRepository {
	
	private Map<String, Exchange> savedExchanges = new HashMap<String, Exchange>();
	
	private String fileName = "c:/users/hugo/desktop/saved_exchanges.txt";
	
	private void saveExchanges(){
		
		StringBuilder sb = new StringBuilder();
		
		for(String key : savedExchanges.keySet()){
			
			Exchange ex = savedExchanges.get(key);
			sb.append(key + "-");
			
		}
		
	}
	
	private void log(String action, String key){
		System.out.println("------------> " + action + " - " + key);
	}

	@Override
	public Exchange add(CamelContext camelContext, String key, Exchange exchange) {
		
		log("add", key);
		
		savedExchanges.put(key, exchange);
		return exchange;
	}

	@Override
	public Exchange get(CamelContext camelContext, String key) {
	 
		log("get", key);
		
		Exchange ex = savedExchanges.get(key);

		return ex;

	}

	@Override
	public void remove(CamelContext camelContext, String key, Exchange exchange) {

		log("remove", key);
		
		savedExchanges.remove(key);
		
	}

	@Override
	public void confirm(CamelContext camelContext, String exchangeId) {
		
		log("confirm", exchangeId);
		
	}

	@Override
	public Set<String> getKeys() {
		
		log("getKeys", "");
		
		return savedExchanges.keySet();

	}

}
