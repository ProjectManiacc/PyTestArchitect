package com.pytestarchitect;

import com.google.gson.Gson;
import okhttp3.*;

import java.awt.*;
import java.util.*;
import java.io.IOException;
import java.util.List;

public class RealAIClient implements AIClient {
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
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", Arrays.asList(
                Collections.singletonMap("role", "system"),
                Collections.singletonMap("role", "user")
        ));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(new HashMap<String, String>() {{
            put("role", "system");
            put("content", "You are a tool that generates Python tests from given source code.");
        }});

        messages.add(new HashMap<String, String>() {{
            put("role", "user");
            put("content", "Generate tests for the following Python code:\n\n" + sourceCode);
        }});


        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        String jsonRequest = gson.toJson(requestBody);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            String responseBody = response.body().string();

            ChatCompletionResponse completion = gson.fromJson(responseBody, ChatCompletionResponse.class);
            if (completion.choices != null && !completion.choices.isEmpty()) {
                return completion.choices.get(0).message.content;
            }
            return null;
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
