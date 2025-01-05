package com.pytestarchitect;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class RealAIClientTest {
    private MockWebServer mockWebServer;
    private RealAIClient client;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        client = new RealAIClient("FAKE_API_KEY");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void testGenerateTestsFromCodeSuccess(){
        String fakeResponse = "{ \"tests\": \"def test_foo(): assert foo() == 42\" }";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200).setBody(fakeResponse));

        String sourceCode = "def foo():\n    return 42";
        String result = client.generateTests(sourceCode);

        assertNotNull(result);
        assertTrue(result.contains("def test_foo():"));
    }

    @Test
    public void testGenerateTestsFromCodeFailure(){

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500).setBody("Internal Server Error"));

        String sourceCode = "def foo():\n    return 42";
        String result = client.generateTests(sourceCode);

        assertNull(result);
    }

    @Test
    public void testInvalidApiKey() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401).setBody("Unauthorized"));

        String result = client.generateTests("def foo():\n    return 42");
        assertEquals("Invalid API key. Please verify your configuration", result);
    }

    @Test
    public void testNoInternetConnection() throws IOException {
        mockWebServer.shutdown();

        String result = client.generateTests("def foo():\n    return 42");
        assertEquals("Unable to connect to API. Check your network connection", result);
    }

    @Test
    public void testEmptyFile() {
        myFixture.configureByText("empty.py", "");

        GenerateTestAction action = new GenerateTestAction();
        myFixture.testAction(action);

        String notification = TestState.getLastNotification();
        assertEquals("No functions or classes detected in the file", notification);
    }

    @Test
    public void testInvalidSyntax() {
        myFixture.configureByText("invalid.py", "def foo(:\n    return 42");

        GenerateTestAction action = new GenerateTestAction();
        myFixture.testAction(action);

        String notification = TestState.getLastNotification();
        assertEquals("Invalid syntax", notification);
    }
}
