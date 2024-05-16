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

    private String accessToken;

    public LineNotifyClient(String accessToken){
        this.accessToken = accessToken;
    }

    public LineNotifyResponse sendNotification(String message) throws IOException {
        // Set the URL
        URL url = new URL("https://notify-api.line.me/api/notify");

        // Open the connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set request method to POST
        conn.setRequestMethod("POST");

        // Set request headers
        conn.setRequestProperty("Authorization", String.format("Bearer " , accessToken));
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Enable input/output
        conn.setDoOutput(true);

        // Construct form data
        String formData = "message=" + message;

        // Write form data to output stream
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = formData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Send the request
        conn.connect();

        // Get the response code
        int responseCode = conn.getResponseCode();

        // Read the response JSON
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            // Parse JSON into LineNotifyResponse object
            LineNotifyResponse notifyResponse = objectMapper.readValue(response.toString(), LineNotifyResponse.class);

            // Disconnect the connection
            conn.disconnect();

            // Print response status code
            System.out.println("Response Status Code: " + responseCode);

            return notifyResponse;
        }
    }
}
