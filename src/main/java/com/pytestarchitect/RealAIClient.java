package com.pytestarchitect;

import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.pytestarchitect.AIBackendTestGenerationService.logger;

public class RealAIClient implements AIClient {
    private static final Logger log = LoggerFactory.getLogger(RealAIClient.class);
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public RealAIClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }


    @Override
    public String generateTests(String sourceCode) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "\"You are an assistant that generates Python unit tests using pytest. \"\n" +
                            "+ \"Return ONLY valid Python code without any additional explanation and without markdown formatting like ```python. . \"\n" +
                            "+ \"Each test case should test a single functionality with one assertion per test. \"\n" +
                            "+ \"If multiple test cases require a shared setup, include a setup method using pytest's fixture system, and include a corresponding teardown step. " +
                            "+ \"Use mocks only when necessary, and avoid including external dependencies unless required. \"\n" +
                            "+ \"Consider edge cases and error conditions in your test cases. \"\n" +
                            "+ \"Remember to use docstrings to describe the purpose of each test case. \"\n" +
                            "+ \"Name the test methods following the pytest naming convention (test_<function name>_<functionality>).\""
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", "Generate pytest test cases for the following Python code:\n\n" + sourceCode
            ));

            Map<String, Object> requestBody = Map.of(
                    "temperature", 0.5,
                    "messages", messages,
                    "model", "gpt-4o-mini"
            );
            String jsonRequest = gson.toJson(requestBody);
            logger.info("Generated JSON Request: " + jsonRequest);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 401) {
                    logger.severe("Invalid API key.");
                    throw new IllegalArgumentException("Invalid API key. Please verify your configuration.");
                }
                if (!response.isSuccessful()) {

                    logger.severe("AI API call failed with response code: " + response.code());
                    logger.severe("AI API call failed with response message: " + response.message());

                    throw new IOException("An error occurred while contacting the API: " + response.message());
                }

                String responseBody = response.body().string();
                logger.info("API Response: " + responseBody);

                ChatCompletionResponse completion = gson.fromJson(responseBody, ChatCompletionResponse.class);

                if (completion.choices == null || completion.choices.isEmpty()) {
                    logger.severe("AI API returned an empty choices list.");
                    throw new IOException("No test cases were generated. API returned an empty response.");
                }
                return completion.choices.get(0).message.content.trim();
            }
        } catch (IOException e) {
            logger.warning("Failed to connect to API: " + e.getMessage());
            throw new RuntimeException("Unable to connect to API. Check your network connection.");
        }

    }

    static class ChatCompletionResponse {
        List<Choice> choices;
    }

    static class Choice {
        Message message;
    }

    static class Message {
        String role;
        String content;
    }


}
