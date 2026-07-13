package com.memoria.Memoria.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnthropicAiServiceImpl implements AiService {

    @Value("${application.ai.anthropic.api-key}")
    private String apiKey;

    @Value("${application.ai.anthropic.model}")
    private String model;

    private final WebClient.Builder webClientBuilder;

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";

    @Override
    public Mono<String> summarize(String content) {
        String prompt = "Please provide a concise summary of the following content for a personal memory system. Focus on key concepts and actionable information:\n\n" + content;
        return callAnthropic(prompt);
    }

    @Override
    public Mono<String> chat(String query, String context) {
        String prompt = "You are Memoria, an intelligent personal memory assistant. Using the following context retrieved from the user's memories, answer their question. If the answer isn't in the context, say so.\n\nContext:\n" + context + "\n\nQuestion: " + query;
        return callAnthropic(prompt);
    }

    private Mono<String> callAnthropic(String prompt) {
        return webClientBuilder.build()
                .post()
                .uri(ANTHROPIC_API_URL)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", model,
                        "max_tokens", 1024,
                        "messages", List.of(
                                Map.of("role", "user", "content", prompt)
                        )
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
                    return (String) content.get(0).get("text");
                });
    }
}
