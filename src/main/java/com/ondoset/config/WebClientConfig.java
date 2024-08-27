package com.ondoset.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

@Log4j2
@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClientAuth() {

		return WebClient.builder()
				.baseUrl("https://apihub.kma.go.kr/api/typ01")
				.filter((request, next) -> {
					log.debug("기상청에 요청된 URL: {}", request.url().toString());
					return next.exchange(request)
							.flatMap(this::handleResponse);
				})
				.build();
	}

	@Bean
	public WebClient webClientService() {

		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("https://apis.data.go.kr/1360000");
		factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

		return WebClient.builder()
				.uriBuilderFactory(factory)
				.filter((request, next) -> {
					log.debug("기상청에 요청된 URL: {}", request.url().toString());
					return next.exchange(request)
							.flatMap(this::handleResponse);
				})
				.build();
	}

	private Mono<ClientResponse> handleResponse(ClientResponse response) {
		if (response.statusCode().isError() || response.headers().header("Content-Type").get(0).startsWith("text/xml")) {
			return response.bodyToMono(String.class)
					.flatMap(body -> {
						log.debug("\n{}", body);
						return Mono.error(new RuntimeException());
					});
		} else {
			return Mono.just(response);
		}
	}

}
