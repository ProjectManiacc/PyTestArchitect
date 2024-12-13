package com.pytestarchitect;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class RealAIClient implements AIClient {
    private final String baseUrl;
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();


    public RealAIClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient();
    }

    @Override
    public String generateTests(String sourceCode) {

        var jsonRequest = gson.toJson(new RequestPayload(sourceCode));

        Request request = new Request.Builder()
                .url(baseUrl + "/generate-tests")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            String responseBody = response.body().string();
            ResponsePayload payload = gson.fromJson(responseBody, ResponsePayload.class);
            return payload.tests;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class RequestPayload {
        String sourceCode;

        public RequestPayload(String sourceCode) {
            this.sourceCode = sourceCode;
        }
    }

    static class ResponsePayload {
        String tests;
    }
}
