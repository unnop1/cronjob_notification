package com.nt.cronjob_notification.client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.cronjob_notification.client.handle.LineNotifyResponse;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class LineNotifyClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public LineNotifyResponse sendNotification(String message, String accessToken) throws IOException {
        try {
            String apiUrl = "https://notify-api.line.me/api/notify";

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setDoOutput(true);

            String postData = "message=" + message;

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            StringBuilder responseMessage = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseMessage.append(responseLine.trim());
                }
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse JSON into LineNotifyResponse object
                LineNotifyResponse notifyResponse = objectMapper.readValue(responseMessage.toString(), LineNotifyResponse.class);
                return notifyResponse;
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
