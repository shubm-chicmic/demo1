package com.example.demo1;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TimeSheetFill {
    public void fillTimeSheet(String empCode) {
        String token = getToken(empCode);
        String url = "https://apigateway.erp.chicmic.in/v1/timesheet/time";
        String notes = getValueFromFile("/home/chicmic/Desktop/erpTimeSheetInput.txt");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, token);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // Construct the request body JSON
            String requestBody = "{\"timeSheetType\": 7, \"timeSpent\": \"07:00\", \"notes\": \"" + notes + "\", \"entryDate\": \"" + LocalDate.now() + "\", \"empId\": \"" + empCode + "\"}";
            System.out.println(requestBody);
            httpPost.setEntity(new StringEntity(requestBody));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                // Process the response if needed
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String getToken(String empcode) {
        String url = "https://apigateway.erp.chicmic.in/v1/auth/login";
        String empCode = "123"; // Replace with your actual employee code
        String token = ""; // Replace with your actual token

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("loginId", "CHM/2023/" + empCode);
            requestBody.put("password", "123456");

            // Set the request body
            StringEntity entity = new StringEntity(requestBody.toString());
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                // Check response status and read the content
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    String newToken = extractValueForKey(responseBody, "accessToken");
                    System.out.println("Response Body: " + responseBody);
                    System.out.println("New Token: " + newToken);
                    return newToken;
                } else {
                    System.out.println("Request failed with status code: " + statusCode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    private static String extractValueForKey(String responseBody, String key) {
        String keyPattern = "\"" + key + "\":\"";
        int startIndex = responseBody.indexOf(keyPattern) + keyPattern.length();
        int endIndex = responseBody.indexOf("\"", startIndex);
        if (startIndex >= 0 && endIndex >= 0) {
            return responseBody.substring(startIndex, endIndex);
        } else {
            return "";
        }
    }
    private static String getValueFromFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

}
