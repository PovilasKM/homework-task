package com.visma.task.consumer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;


@Service
public class RestfulServiceImpl implements RestfulService {

    private final RestTemplate restTemplate;

    @Autowired
    public RestfulServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public <T, U> ResponseEntity<T> postJson(String path, U requestBody, Class<T> responseType, Object... uriVariables) {
        HttpEntity<U> entity = new HttpEntity<>(requestBody, initHttpHeaders(MediaType.APPLICATION_JSON));
        return restTemplate.exchange(path, HttpMethod.POST, entity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> get(String path, Class<T> responseType, Object... uriVariables) {
        HttpEntity<String> entity = new HttpEntity<>(initHttpHeaders());
        return restTemplate.exchange(path, HttpMethod.GET, entity, responseType, uriVariables);
    }

    private HttpHeaders initHttpHeaders(MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        Optional.ofNullable(mediaType).ifPresent(headers::setContentType);
        return headers;
    }

    private HttpHeaders initHttpHeaders() {
        return initHttpHeaders(MediaType.APPLICATION_JSON);
    }

}
