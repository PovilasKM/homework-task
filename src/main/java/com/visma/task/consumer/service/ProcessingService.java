package com.visma.task.consumer.service;

import com.visma.task.consumer.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.SocketException;

@Service
public class ProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessingService.class);

    private final String URL_INIT;
    private final String URL_GET;

    private final RestfulService restfulService;

    @Autowired
    public ProcessingService(@Value("${thirdparty.url.init}") String urlInit, @Value("${thirdparty.url.status}") String urlStatus,
                             RestfulService restfulService) {
        this.URL_INIT = urlInit;
        this.URL_GET = urlStatus;
        this.restfulService = restfulService;
    }

    public String callInit(String content) throws SocketException {
        ResponseEntity<String> response = restfulService.postJson(URL_INIT, content, String.class);
        return response.getBody();
    }

    public Status getStatus(String uuid) throws SocketException {
        ResponseEntity<Status> response = restfulService.get(URL_GET, Status.class, uuid);
        return response.getBody();
    }
}
