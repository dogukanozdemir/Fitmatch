package com.fitmatch.events.client.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class FeignConfig {
  @Bean
  RequestInterceptor authForwarder() {
    return tmpl -> {
      var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs != null) {
        String auth = attrs.getRequest().getHeader("Authorization");
        if (auth != null && !auth.isBlank()) {
          tmpl.header("Authorization", auth);
        }
      }
    };
  }
}
