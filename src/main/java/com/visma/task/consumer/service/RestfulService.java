package com.visma.task.consumer.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

public interface RestfulService {

    <T, U> ResponseEntity<T> postJson(String path, U requestBody, Class<T> responseType, Object... uriVariables);

    <T> ResponseEntity<T> get(String path, ParameterizedTypeReference<T> responseType, Object... uriVariables);

    <T> ResponseEntity<T> get(String path, Class<T> responseType, Object... uriVariables);

    <T, U> ResponseEntity<T> putJson(String path, U requestBody, Class<T> responseType, Object... uriVariables);

    <T, U> ResponseEntity<T> delete(String path, Class<T> responseType, Object... uriVariables);
}
