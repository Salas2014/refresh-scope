package com.salas.spring.boot.env.properties.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class Client {

    @Autowired
    private RestTemplate restTemplate;

    private int port;

    public void callActuatorUrl(String url, Map<String, String> request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> objectHttpEntity = new HttpEntity<>(request, httpHeaders);
        ResponseEntity<String> exchange = restTemplate.exchange(
                "http://localhost:" + url, HttpMethod.POST, objectHttpEntity, String.class
        );

        if (!exchange.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update properties");
        }
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
