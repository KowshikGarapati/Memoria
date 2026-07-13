package com.memoria.Memoria.services;

import reactor.core.publisher.Mono;

public interface AiService {
    Mono<String> summarize(String content);
    Mono<String> chat(String query, String context);
}
