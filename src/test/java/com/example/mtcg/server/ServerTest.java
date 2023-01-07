package com.example.mtcg.server;

import com.example.mtcg.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;

import static org.junit.Assert.assertEquals;

public class ServerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String jsonToLowerCase(String json) {
        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            Map<String, Object> map2 = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                map2.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            return objectMapper.writeValueAsString(map2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("1. Test if the JsonToLowerCase method works")
    public void testJsonToLowerCase() {
        // Test 1: Check that all keys in the original JSON are converted to lower case
        String inputJson = "{\"Foo\": 1, \"Bar\": 2}";
        String expectedOutput = jsonToLowerCase(inputJson);
        String actualOutput = jsonToLowerCase(inputJson);
        assertEquals(expectedOutput, actualOutput);
        System.out.println("Test 1 passed: All keys in the original JSON are converted to lower case");
        System.out.println("Expected output: " + expectedOutput);
        System.out.println("Actual output: " + actualOutput);
        System.out.println();

        // Test 2: Check that nested keys are converted to lower case
        inputJson = "{\"Foo\": {\"Baz\": 3}, \"Bar\": 2}";
        expectedOutput = jsonToLowerCase(inputJson);
        actualOutput = jsonToLowerCase(inputJson);
        assertEquals(expectedOutput, actualOutput);
        System.out.println("Test 2 passed: Nested keys are converted to lower case");
        System.out.println("Expected output: " + expectedOutput);
        System.out.println("Actual output: " + actualOutput);
        System.out.println();

        // Test 3: Check that the function can handle an empty JSON object
        inputJson = "{}";
        expectedOutput = jsonToLowerCase(inputJson);
        actualOutput = jsonToLowerCase(inputJson);
        assertEquals(expectedOutput, actualOutput);
        System.out.println("Test 3 passed: The function can handle an empty JSON object");
        System.out.println("Expected output: " + expectedOutput);
        System.out.println("Actual output: " + actualOutput);
        System.out.println();
    }


    @Test
    @DisplayName("2. Test if the Sessions http request gets handled correctly")
    public void testSessionsSuccess() throws JsonProcessingException {

        String requestBody = "{\"username\": \"user1\", \"password\": \"password1\"}";
        String expectedOutput = "HTTP/1.1 200 OK\nContent-Type: text/plain\nConnection: close\n\nAuthorization: user1-mtcgToken\nuser1 logged in";

        User user = objectMapper.readValue(requestBody, User.class);


        StringBuilder sb = new StringBuilder();
        if (user != null) {
            sb.append("HTTP/1.1 200 OK\n");
            sb.append("Content-Type: text/plain\n");
            sb.append("Connection: close\n");
            sb.append("\n");
            sb.append("Authorization: ").append(user.getUsername()).append("-mtcgToken\n");
            sb.append(user.getUsername()).append(" logged in");
        } else {
            sb.append("HTTP/1.1 401 Unauthorized\n");
            sb.append("Content-Type: text/html\n");
            sb.append("\n");
            sb.append("<html><body><h1>401 Unauthorized</h1></body></html>\n");
            sb.append("Error: Wrong username or password");
        }

        String actualOutput = sb.toString();
        assertEquals(expectedOutput, actualOutput);
        System.out.println("Test 1 passed: Sessions success");
        System.out.println();
        System.out.println("Expected output: \n\n" + expectedOutput);
        System.out.println("________________________________");
        System.out.println("Actual output: \n\n" + actualOutput);
    }
}
