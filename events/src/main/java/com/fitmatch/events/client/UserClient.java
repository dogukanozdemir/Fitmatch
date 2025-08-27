package com.fitmatch.events.client;

import com.fitmatch.events.client.config.FeignConfig;
import com.fitmatch.events.client.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user", path = "/api/users", configuration = FeignConfig.class)
public interface UserClient {

  @GetMapping("/{id}")
  UserDto getById(@PathVariable("id") String id);
}
