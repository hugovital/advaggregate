package com.testes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class RestRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		restConfiguration()
			.component("jetty")
			.host("localhost")
			.port(9080).bindingMode(RestBindingMode.off);

        rest("/say")
        	.get("/hello").to("direct:hello");

        from("direct:hello")
        	.setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
        	.transform().constant("Hello World");
    
        from("direct:bye")
        	.transform().constant("Bye World");		
		
	}

}
