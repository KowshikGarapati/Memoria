package com.memoria.Memoria.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaAiServiceImpl implements AiService {

    @Value("${application.ai.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${application.ai.ollama.summary-model:llama3.2}")
    private String summaryModel;

    @Value("${application.ai.ollama.chat-model:llama3.2}")
    private String chatModel;

    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<String> summarize(String content) {
        if (content == null || content.isBlank()) {
            return Mono.empty();
        }

        String instruction = "You are a concise summarization engine for a personal memory system. "
                + "Provide a clear 2 to 5 sentence summary of the text below. "
                + "Preserve key facts and concepts. "
                + "Do not use markdown, bullet points, or bold text. "
                + "Do not include introductory commentary like 'Here is a summary'. "
                + "If the content is short, output a condensed version directly.\n\nText:\n" + content;

        log.info("Starting AI summary generation using Ollama model [{}]", summaryModel);

        return callOllamaGenerate(summaryModel, instruction)
                .doOnSuccess(summary -> log.info("Ollama summary received successfully for model [{}]", summaryModel))
                .doOnError(e -> log.error("Ollama summary generation failed for model [" + summaryModel + "]", e));
    }

    @Override
    public Mono<String> chat(String query, String context) {
        String prompt = "You are Memoria, an intelligent personal memory assistant. "
                + "Using the following context retrieved from the user's memories, answer their question. "
                + "If the answer isn't in the context, say so.\n\nContext:\n" + context + "\n\nQuestion: " + query;

        log.info("Starting AI chat query using Ollama model [{}]", chatModel);

        return callOllamaGenerate(chatModel, prompt)
                .doOnSuccess(ans -> log.info("Ollama chat response received successfully"))
                .doOnError(e -> log.error("Ollama chat query failed", e));
    }

    private Mono<String> callOllamaGenerate(String targetModel, String prompt) {
        log.debug("Sending POST request to Ollama /api/generate at {}", ollamaUrl);

        return webClientBuilder.build()
                .post()
                .uri(ollamaUrl + "/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", targetModel,
                        "prompt", prompt,
                        "stream", false
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String text = (String) response.get("response");
                    return text != null ? text.trim() : "";
                });
    }
}
