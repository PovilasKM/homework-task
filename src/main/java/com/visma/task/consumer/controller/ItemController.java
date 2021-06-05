package com.visma.task.consumer.controller;

import com.visma.task.consumer.model.Status;
import com.visma.task.consumer.model.StatusType;
import com.visma.task.consumer.service.ItemService;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "api/items", produces = "application/json")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);


    private ItemService itemService;

    private final WebClient webClient;
    private final CloseableHttpAsyncClient apacheClient;

    @Autowired
    public ItemController(@Value("${thirdparty.url.base}") String baseUrl, ItemService itemService) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.itemService = itemService;
        this.apacheClient = HttpAsyncClients.custom().setMaxConnPerRoute(2000).setMaxConnTotal(2000).build();
        this.apacheClient.start();
    }

    @PostMapping("/{content}")
    public ResponseEntity getItemStatus(@PathVariable String content) throws Exception {
        String uuid = itemService.createItem(content);
        StatusType statusType;
        do {
            statusType = itemService.getStatusType(uuid);
            if (statusType.equals(StatusType.FAILED)) {
                uuid = itemService.createItem(content);
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

    @PostMapping("/v3/{content}")
    public Mono<ResponseEntity<Status>> getItemStatusFluxApache(@PathVariable String content) {
        return Mono.fromCompletionStage(sendRequestWithApacheHttpClient()).flatMap(e ->
                Mono.fromCompletionStage(sendRequestForStatus(e)).retryWhen(Retry.backoff(  10, Duration.ofSeconds(1))))
                .map(e -> {
                    logger.info("POTATO, {}", e);
                    return ResponseEntity.ok().body(e);
                });
    }

    private CompletableFuture<String> sendRequestWithApacheHttpClient() {
        CompletableFuture<org.apache.http.HttpResponse> cf = new CompletableFuture<>();
        FutureCallback<HttpResponse> callback = new HttpResponseCallback(cf);
        HttpUriRequest request = new HttpPost("http://localhost:8037/thirdpartyservice/init");
        apacheClient.execute(request, callback);
        return cf.thenApply(response -> {
            try {
                return EntityUtils.toString(response.getEntity());
            } catch (ParseException | IOException e) {
                return e.toString();
            }
        }).exceptionally(e -> null);
    }

    private CompletableFuture<Status> sendRequestForStatus(String uuid) {
        CompletableFuture<org.apache.http.HttpResponse> cf = new CompletableFuture<>();
        FutureCallback<HttpResponse> callback = new HttpResponseCallback(cf);
        HttpUriRequest request = new HttpGet("http://localhost:8037/thirdpartyservice/checkStatus/" + uuid);
        apacheClient.execute(request, callback);
        return cf.thenApply(response -> (Status) response.getEntity()).exceptionally(e -> null);
    }
}

class HttpResponseCallback implements FutureCallback<org.apache.http.HttpResponse> {

    private CompletableFuture<org.apache.http.HttpResponse> cf;

    HttpResponseCallback(CompletableFuture<org.apache.http.HttpResponse> cf) {
        this.cf = cf;
    }

    @Override
    public void failed(Exception ex) {
        cf.completeExceptionally(ex);
    }

    @Override
    public void completed(org.apache.http.HttpResponse result) {
        cf.complete(result);
    }

    @Override
    public void cancelled() {
        cf.completeExceptionally(new Exception("Cancelled by http async client"));
    }
}