package com.memoria.Memoria.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaEmbeddingService implements EmbeddingService {

    @Value("${application.ai.ollama.url}")
    private String ollamaUrl;

    @Value("${application.ai.ollama.model}")
    private String model;

    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<float[]> getEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return Mono.empty();
        }

        return webClientBuilder.build()
                .post()
                .uri(ollamaUrl + "/api/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", model,
                        "input", text
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<List<Double>> embeddings = (List<List<Double>>) response.get("embeddings");
                    List<Double> vector = embeddings.get(0);
                    float[] floatVector = new float[vector.size()];
                    for (int i = 0; i < vector.size(); i++) {
                        floatVector[i] = vector.get(i).floatValue();
                    }
                    return floatVector;
                })
                .doOnSuccess(e -> log.info("Ollama embedding generated successfully"))
                .doOnError(e -> log.error("Ollama embedding failed", e))
                .onErrorResume(e -> {
                    log.warn("Ollama embedding service unavailable: {}", e.getMessage());
                    return Mono.empty();
                });
    }
}
