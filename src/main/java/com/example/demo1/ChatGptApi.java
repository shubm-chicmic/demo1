package com.example.demo1;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class ChatGptApi {
    private static final String API_KEY = "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";
    private static final String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";

    public static String generateAnswer(String question) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_ENDPOINT);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + API_KEY);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);

            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", question);

            requestBody.append("messages", message);

            httpPost.setEntity(new StringEntity(requestBody.toString()));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    JSONObject jsonResponse = new JSONObject(EntityUtils.toString(entity));
                    System.out.println("Response: " + jsonResponse.toString());
                    return jsonResponse.getJSONArray("choices").getJSONObject(0).getString("message").toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "An error occurred while generating the answer.";
    }
}
