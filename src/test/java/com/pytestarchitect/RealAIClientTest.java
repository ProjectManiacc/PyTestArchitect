package com.pytestarchitect;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RealAIClientTest {
    private MockWebServer mockWebServer;
    private RealAIClient client;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        client = new RealAIClient(mockWebServer.url("/").toString(), "FAKE_API_KEY");
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

        assertNotNull(result);
    }
}
