package com.pytestarchitect;

import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RealAIClient implements AIClient {
    private static final Logger log = LoggerFactory.getLogger(RealAIClient.class);
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    protected String getApiUrl() {
        return API_URL;
    }

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
        Map<String, Object> requestBody = createRequestBody(sourceCode);
        String jsonRequest = serializeRequest(requestBody);
        Request request = buildHttpRequest(jsonRequest);

        try (Response response = executeRequest(request)) {
            validateResponse(response);
            String responseBody = extractResponseBody(response);
            return parseResponse(responseBody);
        } catch (IOException e) {
            logAndThrowConnectionError(e);
        }
        return null;
    }

    Map<String, Object> createRequestBody(String sourceCode) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "You are an assistant that generates Python unit tests using pytest. " +
                        "Return ONLY valid Python code without any additional explanation and without markdown formatting like ```python. " +
                        "Each test case should test a single functionality with one assertion per test. " +
                        "If multiple test cases require a shared setup, include a setup method using pytest's fixture system, " +
                        "and include a corresponding teardown step. Use mocks only when necessary, " +
                        "and avoid including external dependencies unless required. Consider edge cases and error conditions " +
                        "in your test cases. Remember to use docstrings to describe the purpose of each test case. " +
                        "Name the test methods following the pytest naming convention (test_<function name>_<functionality>)."
        ));
        messages.add(Map.of(
                "role", "user",
                "content", "Generate pytest test cases for the following Python code:\n\n" + sourceCode
        ));

        return Map.of(
                "temperature", 0.5,
                "messages", messages,
                "model", "gpt-4o-mini"
        );
    }

    String serializeRequest(Map<String, Object> requestBody) {
        String jsonRequest = gson.toJson(requestBody);
        log.info("Generated JSON Request: {}", jsonRequest);
        return jsonRequest;
    }

    Request buildHttpRequest(String jsonRequest) {
        return new Request.Builder()
                .url(getApiUrl())
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                .build();
    }

    Response executeRequest(Request request) throws IOException {
        return httpClient.newCall(request).execute();
    }

    void validateResponse(Response response) throws IOException {
        if (response.code() == 401) {
            log.error("Invalid API key.");
            throw new IllegalArgumentException("Invalid API key. Please verify your configuration.");
        }
        if (!response.isSuccessful()) {
            log.error("AI API call failed with response code: {}", response.code());
            throw new IOException("An error occurred while contacting the API: " + response.message());
        }
    }

    String extractResponseBody(Response response) throws IOException {
        if (response.body() == null) {
            log.error("Response body is null.");
            throw new IOException("Empty response body received from the API.");
        }
        String responseBody = response.body().string();
        log.info("API Response: {}", responseBody);
        return responseBody;
    }

    String parseResponse(String responseBody) throws IOException {
        ChatCompletionResponse completion = gson.fromJson(responseBody, ChatCompletionResponse.class);

        if (completion.choices == null || completion.choices.isEmpty()) {
            log.error("AI API returned an empty choices list.");
            throw new IOException("No test cases were generated. API returned an empty response.");
        }
        return completion.choices.get(0).message.content.trim();
    }

    private void logAndThrowConnectionError(IOException e) {
        log.warn("Failed to connect to API: {}", e.getMessage());
        throw new RuntimeException("Unable to connect to API. Check your network connection.", e);
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
