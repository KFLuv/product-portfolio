package com.iflytek.chatgpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * AI 对话服务：调用 OpenAI 兼容的 chat/completions 接口
 * <p>
 * 智谱 GLM、讯飞星火、硅基流动、阿里百炼、DeepSeek、Groq 等都兼容这套协议，
 * 只要在 application.properties 里改 ai.base-url / ai.api-key / ai.model 即可切换。
 */
@Service
public class AiChatService {

    /** 旅游助手人设，让模型别答成通用百科 */
    private static final String SYSTEM_PROMPT =
            "你是「旅行助手」，一位专业的中文旅行规划顾问。"
                    + "请用中文回答，语气亲切专业，条理清晰。"
                    + "涉及行程时按天给出安排，并适当给出交通、美食、注意事项方面的建议。"
                    + "如果用户的问题与旅游无关，也可以正常回答，但要简短。";

    @Value("${ai.base-url:}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 三项配置都填了才算可用，否则调用方走本地模拟回复
     */
    public boolean isConfigured() {
        return !isBlank(baseUrl) && !isBlank(apiKey) && !isBlank(model);
    }

    /**
     * 发一条消息给大模型，返回纯文本回复
     */
    public String chat(String userMessage) throws IOException, InterruptedException {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0.7);

        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", SYSTEM_PROMPT);
        messages.addObject().put("role", "user").put("content", userMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stripTrailingSlash(baseUrl) + "/chat/completions"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("AI 接口返回 HTTP " + response.statusCode() + "：" + response.body());
        }

        JsonNode content = objectMapper.readTree(response.body())
                .path("choices").path(0).path("message").path("content");

        if (content.isMissingNode() || content.asText().isEmpty()) {
            throw new IllegalStateException("AI 接口没有返回内容：" + response.body());
        }
        return content.asText();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String stripTrailingSlash(String url) {
        return url.trim().replaceAll("/+$", "");
    }
}
