package hlquery;

import hlquery.utils.Auth;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

public class Request {
    private final String baseUrl;
    private final int timeout;
    private final Auth auth;
    private final HttpClient httpClient;

    public Request(String baseUrl, int timeout, String token, String authMethod) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.auth = new Auth(token, authMethod);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
    }

    public void setAuthToken(String token, String method) {
        this.auth.setToken(token, method);
    }

    public void clearAuth() {
        this.auth.clear();
    }

    public Response execute(String method, String path, Object body, Map<String, String> queryParams) {
        try {
            String url = baseUrl + path;
            if (queryParams != null && !queryParams.isEmpty()) {
                StringJoiner joiner = new StringJoiner("&", "?", "");
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                }
                url += joiner.toString();
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeout))
                    .header("Accept", "application/json");

            if (auth.hasAuth()) {
                if ("api-key".equalsIgnoreCase(auth.getMethod())) {
                    builder.header("X-API-Key", auth.getToken());
                } else {
                    builder.header("Authorization", "Bearer " + auth.getToken());
                }
            }

            HttpRequest.BodyPublisher bodyPublisher;
            if (body == null) {
                bodyPublisher = HttpRequest.BodyPublishers.noBody();
            } else {
                builder.header("Content-Type", "application/json");
                String jsonBody;
                if (body instanceof JSONObject || body instanceof org.json.JSONArray) {
                    jsonBody = body.toString();
                } else if (body instanceof String) {
                    jsonBody = (String) body;
                } else {
                    jsonBody = new JSONObject(body).toString();
                }
                bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonBody);
            }

            builder.method(method, bodyPublisher);

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new Response(response.statusCode(), response.body(), response.headers().map());

        } catch (Exception e) {
            throw new RuntimeException("Request failed: " + e.getMessage(), e);
        }
    }

    public Response execute(String method, String path) {
        return execute(method, path, null, null);
    }

    public Response execute(String method, String path, Object body) {
        return execute(method, path, body, null);
    }
}
