package com.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.userservice.error.FeignErrorDecoder;

@Configuration
public class DecoderConfig {

	// @Bean
	// public FeignErrorDecoder getFeignErrorDecoder() {
	// 	return new FeignErrorDecoder();
	// }
}
