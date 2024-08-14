package com.ondoset.config;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
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
				.filter((request, next) ->
						next.exchange(request)
								.flatMap(this::handleResponse))
				.build();
	}

	@Bean
	public WebClient webClientService() {

		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("https://apis.data.go.kr/1360000");
		factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

		return WebClient.builder()
				.uriBuilderFactory(factory)
				.filter((request, next) ->
						next.exchange(request)
								.flatMap(this::handleResponse))
				.build();
	}

	private Mono<ClientResponse> handleResponse(ClientResponse response) {
		if (response.statusCode().isError()) {
			return response.bodyToMono(String.class)
					.flatMap(body -> {
						log.error(body);
						return Mono.error(new CustomException(ResponseCode.COM5000));
					});
		} else if (response.headers().header("Content-Type").get(0).startsWith("text/xml")) {
			return response.bodyToMono(String.class)
					.flatMap(body -> {
						log.debug(body);
						return Mono.error(new RuntimeException("type error"));
					});
		} else {
			return Mono.just(response);
		}
	}

}
