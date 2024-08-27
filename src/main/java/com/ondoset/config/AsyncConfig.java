package com.ondoset.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

	@Bean
	public Executor threadPoolTaskExecutor() {

		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10); // 기본 스레드 수
		executor.setMaxPoolSize(30); // 최대 스레드 수
		return executor;
	}

	@Override
	public Executor getAsyncExecutor() {

		return threadPoolTaskExecutor();
	}
}
