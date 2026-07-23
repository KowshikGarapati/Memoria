package com.memoria.Memoria.services;

import reactor.core.publisher.Mono;

public interface EmbeddingService {
    Mono<float[]> getEmbedding(String text);
}
