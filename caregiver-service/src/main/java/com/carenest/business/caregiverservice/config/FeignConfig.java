package com.carenest.business.caregiverservice.config;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import feign.codec.Decoder;
import feign.codec.Encoder;

@Configuration
public class FeignConfig {
	@Bean
	public Decoder feignDecoder() {
		ObjectFactory<HttpMessageConverters> factory =
			() -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
		return new ResponseEntityDecoder(new SpringDecoder(factory));
	}
	@Bean
	public Encoder feignEncoder() {
		ObjectFactory<HttpMessageConverters> factory =
			() -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
		return new SpringEncoder(factory);
	}
}
