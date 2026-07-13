package com.memoria.Memoria.services;

import reactor.core.publisher.Mono;

public interface EmbeddingService {
    Mono<double[]> getEmbedding(String text);
}
