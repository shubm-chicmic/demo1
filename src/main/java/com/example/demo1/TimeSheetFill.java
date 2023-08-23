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
    public String fillTimeSheet(String empCode, String empTime) {
        String token = getToken(empCode);
        String url = "https://apigateway.erp.chicmic.in/v1/timesheet/time";
        String notes = getValueFromFile("/home/chicmic/Desktop/erpTimeSheetInput.txt");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, token);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            String formattedTimeSpent = String.format("0%s:00", empTime);

// Construct the request body JSON with the formatted time
            String requestBody = "{\"timeSheetType\": 7, \"timeSpent\": \"" + formattedTimeSpent + "\", \"notes\": \"" + notes + "\", \"entryDate\": \"" + LocalDate.now() + "\"}";

            System.out.println("Request Body of main : " + requestBody);
            httpPost.setEntity(new StringEntity(requestBody));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                // Process the response if needed
                if (response.getStatusLine().getStatusCode() == 200) {
                    System.out.println("\u001B[35m Time Sheet filled successfully \u001B[0m");
                    return "Timesheet Fill SuccessFully";

                }else {
                    return response.toString();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private String getToken(String empCode) {
        String url = "https://apigateway.erp.chicmic.in/v1/auth/login";

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            String loginId = "CHM/2023/" + empCode;
            String password = "123456";

            // Construct the request body JSON
            String requestBody = "{\"loginId\": \"" + loginId + "\", \"password\": \"" + password + "\"}";
            System.out.println(requestBody);

            httpPost.setEntity(new StringEntity(requestBody));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                // Check response status and read the content
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    String newToken = extractValueForKey(responseBody, "accessToken");

//                    System.out.println("New Token: " + newToken);
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
