package com.pytestarchitect;

import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.io.IOException;
import java.util.List;

import static com.pytestarchitect.AIBackendTestGenerationService.logger;

public class RealAIClient implements AIClient {
    private static final Logger log = LoggerFactory.getLogger(RealAIClient.class);
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public RealAIClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient();
    }


    @Override
    public String generateTests(String sourceCode) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "You are a tool that generates Python unit tests using pytest. Ensure tests cover all methods and edge cases."
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", "Generate pytest test cases for the following Python code:\n\n" + sourceCode
            ));

            Map<String, Object> requestBody = Map.of(
                    "temperature", 0.7,
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

                if (!response.isSuccessful()) {

                    logger.severe("AI API call failed with response code: " + response.code());
                    logger.severe("AI API call failed with response message: " + response.message());

                    return null;
                }

                String responseBody = response.body().string();
                logger.info("API Response: " + responseBody);

                ChatCompletionResponse completion = gson.fromJson(responseBody, ChatCompletionResponse.class);

                if (completion.choices == null || completion.choices.isEmpty()) {
                    logger.severe("AI API returned an empty choices list.");
                    return null;
                }
                return completion.choices.get(0).message.content.trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
