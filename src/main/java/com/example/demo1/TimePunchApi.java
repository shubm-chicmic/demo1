package com.example.demo1;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import org.json.JSONObject;
import org.json.JSONArray;
public class TimePunchApi {
    public static TimePunch timePunchApiCall(String empCode) {
        System.out.println("\u001B[36m Time Punch API call\u001B[0m");
        TimePunch timePunch = new TimePunch();
        String url = "https://apigateway.erp.chicmic.in/v1/biometric/punches";
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2M2NlN2JmZjkzZTkxMzA2N2QwMmNlOGQiLCJlbWFpbCI6InNodWIubWlzaHJhMjIxMEBnbWFpbC5jb20iLCJ0aW1lIjoxNjkxNDg1OTUwMjY5LCJpYXQiOjE2OTE0ODU5NTB9.NazGmjzozuxoMJlPg7nbfYXmXOgOlXjMtwl95Saesiw";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, token); // Assuming 'token' is defined
            LocalDate date = LocalDate.now();

            String requestBody = "{\"date\": \"" + date.toString() + "\", \"empId\": \"" + empCode + "\"}";
            httpPost.setEntity(new StringEntity(requestBody));
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity responseEntity = response.getEntity();
                    String responseBody = EntityUtils.toString(responseEntity);

                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray dataArray = jsonResponse.getJSONArray("data");

                    // Get the last object in the data array
                    JSONObject lastDataObject = dataArray.getJSONObject(dataArray.length() - 1);

                    // Now you can access the properties of the last data object
                    String punchType = lastDataObject.getString("devDirection");
                    String id = lastDataObject.getString("_id");

                    timePunch.setPunchType(punchType);
                    timePunch.setId(id);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return timePunch;


    }
}
