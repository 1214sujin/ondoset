package com.ondoset.config;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import io.netty.channel.ChannelOption;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

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
							.flatMap(this::handleResponse)
							.retryWhen(Retry.backoff(3, Duration.ofMillis(100))
									.doBeforeRetry(retrySignal -> log.info("Retrying..."))
									.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new CustomException(ResponseCode.KMA5000))
							)
							.doOnError(e -> {
								log.error("오류가 발생한 요청 API: {}", request.url().toString());
								log.error(e);
							});
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
							.flatMap(this::handleResponse)
							.retryWhen(Retry.backoff(3, Duration.ofMillis(100))
									.doBeforeRetry(retrySignal -> log.info("Retrying..."))
									.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new CustomException(ResponseCode.KMA5000))
							)
							.doOnError(e -> {
								log.error("오류가 발생한 요청 API: {}", request.url().toString());
								log.error(e);
							});
				})
				.build();
	}

	private Mono<ClientResponse> handleResponse(ClientResponse response) {
		if (response.statusCode().isError() || response.headers().header("Content-Type").get(0).startsWith("text/xml")) {
			return response.bodyToMono(String.class)
					.flatMap(body -> {
						log.error("오류 응답 발생:\n{}", body);
						return Mono.error(new RuntimeException());
					});
		} else {
			return Mono.just(response);
		}
	}
}
