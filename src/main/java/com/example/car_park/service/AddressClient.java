package com.example.car_park.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class AddressClient {
    private static final String API_KEY = "9bc9ddf0750f4372af128cc29d52c719";
    private static final String BASE_URL = "https://api.geoapify.com/v1/geocode/reverse?";
    private static final String FORMAT = "json";

    public String getAddressByCoords(double longitude, double latitude) {
        final String url = String.format("%slat=%s&lon=%s&format=%s&apiKey=%s",
                BASE_URL,
                latitude,
                longitude,
                FORMAT,
                API_KEY);
        JSONObject response = sendGetRequest(url);
        JSONObject info = response.getJSONArray("results").getJSONObject(0);
        return String.format("%s, %s, %s, %s, %s",
                info.getString("country"),
                info.getString("state"),
                info.getString("city"),
                info.getString("street"),
                info.getString("housenumber"));
    }

    private JSONObject sendGetRequest(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return new JSONObject(response.toString());
        } catch (IOException e) {
            return new JSONObject();
        }
    }
}
