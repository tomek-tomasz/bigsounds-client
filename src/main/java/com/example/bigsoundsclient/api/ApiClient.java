package com.example.bigsoundsclient.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    public static final String BASE_URL = "http://127.0.0.1:3000";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static final Gson GSON = new Gson();

    public record ApiResponse(boolean ok, int status, JsonElement data) {}

    private static ApiResponse request(String method, String path, Object body) {
        try {
            String token = AuthState.getInstance().getToken();

            String bodyStr = body != null ? GSON.toJson(body) : "";
            HttpRequest.BodyPublisher publisher = body != null
                    ? HttpRequest.BodyPublishers.ofString(bodyStr)
                    : HttpRequest.BodyPublishers.noBody();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .method(method, publisher);

            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }

            HttpResponse<String> res = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            JsonElement data;
            try {
                data = JsonParser.parseString(res.body());
            } catch (Exception e) {
                data = GSON.toJsonTree(res.body());
            }

            return new ApiResponse(res.statusCode() < 300, res.statusCode(), data);
        } catch (Exception e) {
            return new ApiResponse(false, 0, GSON.toJsonTree("Error: " + e.getMessage()));
        }
    }

    public static ApiResponse get(String path)                { return request("GET",    path, null); }
    public static ApiResponse post(String path, Object body)  { return request("POST",   path, body); }
    public static ApiResponse patch(String path, Object body) { return request("PATCH",  path, body); }
    public static ApiResponse delete(String path)             { return request("DELETE", path, null); }
}
