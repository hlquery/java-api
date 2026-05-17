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
import java.util.Collections;
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
                    if (entry.getKey() == null || entry.getValue() == null) {
                        continue;
                    }
                    joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                }
                String query = joiner.toString();
                if (!"?".equals(query)) {
                    url += query;
                }
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

    /**
     * Non-throwing request variant. On failure, returns a Response with statusCode=0
     * and a small JSON error payload in the body.
     */
    public Response executeSafe(String method, String path, Object body, Map<String, String> queryParams) {
        try {
            return execute(method, path, body, queryParams);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            String json = new JSONObject()
                    .put("error", "Request failed")
                    .put("message", message)
                    .toString();
            return new Response(0, json, Collections.emptyMap());
        }
    }

    /**
     * Simple retry helper for transient failures. Retries on exceptions and on 502/503/504.
     */
    public Response executeWithRetry(
            String method,
            String path,
            Object body,
            Map<String, String> queryParams,
            int maxRetries,
            long initialBackoffMillis
    ) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0");
        }
        if (initialBackoffMillis < 0) {
            throw new IllegalArgumentException("initialBackoffMillis must be >= 0");
        }

        long backoff = initialBackoffMillis;
        int attempts = 0;
        while (true) {
            try {
                Response response = execute(method, path, body, queryParams);
                int code = response.getStatusCode();
                if ((code == 502 || code == 503 || code == 504) && attempts < maxRetries) {
                    if (backoff > 0) Thread.sleep(backoff);
                    backoff = Math.min(backoff * 2, 10_000L);
                    attempts++;
                    continue;
                }
                return response;
            } catch (Exception e) {
                if (attempts >= maxRetries) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException("Request failed after retries: " + e.getMessage(), e);
                }
                try {
                    if (backoff > 0) Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
                backoff = Math.min(backoff * 2, 10_000L);
                attempts++;
            }
        }
    }

    public Response execute(String method, String path) {
        return execute(method, path, null, null);
    }

    public Response execute(String method, String path, Object body) {
        return execute(method, path, body, null);
    }
}
