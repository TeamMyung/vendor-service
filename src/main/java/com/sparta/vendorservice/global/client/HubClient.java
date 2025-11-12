package com.sparta.vendorservice.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-service", url = "http://localhost:8082")
public interface HubClient {
    @GetMapping("/v1/hubs/api/{hubId}")
    boolean existsHub(@PathVariable("hubId") UUID hubId);

    @GetMapping("/v1/hubs/api/{hubId}/name")
    String getHubName(@PathVariable("hubId") UUID hubId);
}
