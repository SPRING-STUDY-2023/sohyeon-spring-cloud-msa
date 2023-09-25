package com.example.apigatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

	@Override
	public GatewayFilter apply(Config config) {
		// Custom Pre filter
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();

			log.info("Custom Pre filter: request id = {}", request.getId());

			// Custom Post filter
			return chain.filter(exchange)
				.then(Mono.fromRunnable(
					() -> log.info("Custom Post filter: respose code = {}", response.getStatusCode())));
		};
	}

	public CustomFilter() {
		super(Config.class);
	}

	public static class Config {
		// Put the configuration properties
	}

}
