package com.pytestarchitect;

import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RealAIClientTest {

    private MockWebServer mockWebServer;
    private RealAIClient realAIClient;
    private final String validApiKey = "valid_api_key";

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();


        realAIClient = new RealAIClient(validApiKey) {
            @Override
            protected String getApiUrl() {
                return mockWebServer.url("/v1/chat/completions").toString();
            }
        };
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testCreateRequestBody() {
        String sourceCode = "def example(): pass";
        Map<String, Object> requestBody = realAIClient.createRequestBody(sourceCode);

        assertNotNull("Request body should not be null", requestBody);
        assertEquals("Request body should contain correct temperature", 0.5, requestBody.get("temperature"));
        assertTrue("Request body should contain messages",
                requestBody.get("messages") instanceof List);
        assertEquals("Request body should use correct model", "gpt-4o-mini", requestBody.get("model"));
    }

    @Test
    public void testSerializeRequest() {
        String sourceCode = "def example(): pass";
        Map<String, Object> requestBody = realAIClient.createRequestBody(sourceCode);
        String jsonRequest = realAIClient.serializeRequest(requestBody);

        assertNotNull("Serialized JSON request should not be null", jsonRequest);
        assertTrue("Serialized request should contain 'temperature'", jsonRequest.contains("\"temperature\":0.5"));
        assertTrue("Serialized request should contain 'messages'", jsonRequest.contains("\"messages\""));
    }

    @Test
    public void testBuildHttpRequest() {
        String jsonRequest = "{}";
        Request request = realAIClient.buildHttpRequest(jsonRequest);

        assertNotNull("Request should not be null", request);
        assertEquals("Request method should be POST", "POST", request.method());
        assertEquals("Request content type should be application/json",
                "application/json; charset=utf-8", request.body().contentType().toString());
    }

    @Test
    public void testExecuteRequest() throws IOException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"key\": \"value\"}")
                .setResponseCode(200));

        Request request = realAIClient.buildHttpRequest("{}");
        Response response = realAIClient.executeRequest(request);

        assertNotNull("Response should not be null", response);
        assertEquals("Response code should be 200", 200, response.code());
    }

    @Test
    public void testValidateResponseSuccess() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody("{}").setResponseCode(200));

        Request request = realAIClient.buildHttpRequest("{}");
        Response response = realAIClient.executeRequest(request);

        realAIClient.validateResponse(response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateResponseInvalidApiKey() throws IOException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        Request request = realAIClient.buildHttpRequest("{}");
        Response response = realAIClient.executeRequest(request);

        realAIClient.validateResponse(response);
    }

    @Test(expected = IOException.class)
    public void testValidateResponseFailure() throws IOException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Request request = realAIClient.buildHttpRequest("{}");
        Response response = realAIClient.executeRequest(request);

        realAIClient.validateResponse(response);
    }

    @Test
    public void testExtractResponseBody() throws IOException {
        String responseBody = "{\"key\": \"value\"}";
        mockWebServer.enqueue(new MockResponse().setBody(responseBody).setResponseCode(200));

        Request request = realAIClient.buildHttpRequest("{}");
        Response response = realAIClient.executeRequest(request);

        String extractedBody = realAIClient.extractResponseBody(response);
        assertNotNull("Extracted body should not be null", extractedBody);
        assertEquals("Extracted body should match the response", responseBody, extractedBody);
    }

    @Test
    public void testParseResponse() throws IOException {
        String responseBody = """
        {
            "choices": [
                {
                    "message": {
                        "role": "assistant",
                        "content": "def test_example():\\n    assert True"
                    }
                }
            ]
        }
        """;
        mockWebServer.enqueue(new MockResponse().setBody(responseBody).setResponseCode(200));

        Request request = realAIClient.buildHttpRequest("{}");
        Response response = realAIClient.executeRequest(request);
        String parsedResponse = realAIClient.parseResponse(response.body().string());

        assertNotNull("Parsed response should not be null", parsedResponse);
        assertTrue("Parsed response should contain test content", parsedResponse.contains("def test_example():"));
    }

    @Test(expected = IOException.class)
    public void testParseResponseEmptyChoices() throws IOException {
        String responseBody = "{\"choices\": []}";
        mockWebServer.enqueue(new MockResponse().setBody(responseBody).setResponseCode(200));

        Request request = realAIClient.buildHttpRequest("{}");
        Response response = realAIClient.executeRequest(request);
        realAIClient.parseResponse(response.body().string()); // Should throw IOException
    }
}
