package com.memoria.Memoria.services;

import reactor.core.publisher.Mono;

public interface EmbeddingService {
    Mono<float[]> getEmbedding(String text);

    /**
     * Synchronously retrieves a text embedding vector, returning null on error or empty text.
     * Encapsulates blocking behavior for imperative Spring MVC services.
     */
    default float[] getEmbeddingSync(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return getEmbedding(text).block();
        } catch (Exception e) {
            return null;
        }
    }
}
