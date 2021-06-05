package com.visma.task.consumer.controller;

import com.visma.task.consumer.model.Status;
import com.visma.task.consumer.model.StatusType;
import com.visma.task.consumer.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@RestController
@RequestMapping(value = "api/items", produces = "application/json")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);


    private ItemService itemService;

    private final WebClient webClient;

    @Autowired
    public ItemController(@Value("${thirdparty.url.base}") String baseUrl, ItemService itemService) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.itemService = itemService;
    }

    @PostMapping("/{content}")
    public ResponseEntity getItemStatus(@PathVariable String content) throws Exception {
        String uiid = itemService.createItem(content);
        StatusType statusType;
        do {
            statusType = itemService.getStatusType(uiid);
            if (statusType.equals(StatusType.FAILED)) {
                uiid = itemService.createItem(content);
            }
        } while (!statusType.equals(StatusType.OK));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/v2/{content}")
    public Mono<ResponseEntity<Status>> getItemStatusFlux(@PathVariable String content) {
        return getUuidAndStatus().map(e -> ResponseEntity.ok().body(e));
    }

    private Mono<Status> getUuidAndStatus() {
        return getUuid().flatMap(this::getOkStatus);
    }

    private Mono<String> getUuid() {
        return webClient.post().uri("/init").retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(10, Duration.ofSeconds(1)));
    }

    private Mono<Status> getOkStatus(String uuid) {
        return getStatus(uuid).flatMap(e -> {
            //logger.info("New Status for {}: {}", uuid, e);
            if (e.getStatusType().equals(StatusType.IN_PROGRESS)) {
                return getOkStatus(uuid);
            }  if (e.getStatusType().equals(StatusType.FAILED)) {
                return getUuidAndStatus();
            } else {
                return Mono.just(e);
            }
        });
    }

    private Mono<Status> getStatus(String uuid) {
        return webClient.get().uri("/checkStatus/{uuid}", uuid).retrieve()
                .bodyToMono(Status.class)
                .retryWhen(Retry.backoff(  10, Duration.ofSeconds(1)));
    }
}
