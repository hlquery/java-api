package hlquery;

import hlquery.utils.Config;
import hlquery.utils.Validator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Client {
    private final Request request;
    private final Collections collections;
    private final Documents documents;
    private final Search search;

    public Client(String baseUrl) {
        this(baseUrl, new HashMap<>());
    }

    public Client(String baseUrl, Map<String, Object> options) {
        options = Config.mergeDefaults(options);
        
        String url = (baseUrl != null) ? baseUrl : (String) options.get("base_url");
        url = Config.normalizeUrl(url);
        
        if (!Config.isValidUrl(url)) {
            throw new IllegalArgumentException("Invalid base URL: " + url);
        }
        
        int timeout = (int) options.get("timeout");
        String token = (String) options.get("token");
        String authMethod = (String) options.get("auth_method");
        
        this.request = new Request(url, timeout, token, authMethod);
        this.collections = new Collections(this.request);
        this.documents = new Documents(this.request);
        this.search = new Search(this.request, this.collections);
    }

    public void setAuthToken(String token, String method) {
        this.request.setAuthToken(token, method);
    }

    public void clearAuth() {
        this.request.clearAuth();
    }

    // Cluster & Node APIs
    public Response health() {
        return request.execute("GET", "/health");
    }

    public Response stats() {
        return request.execute("GET", "/stats");
    }

    public Response etc() {
        return request.execute("GET", "/etc");
    }

    public Response sql(String sql) {
        return sql(sql, new HashMap<>());
    }

    public Response sql(String sql, Map<String, String> queryParams) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query must be a non-empty string");
        }

        Map<String, String> params = new HashMap<>();
        if (queryParams != null) {
            params.putAll(queryParams);
        }
        params.put("sql", sql);
        return request.execute("GET", "/sql", null, params);
    }

    public Response execSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query must be a non-empty string");
        }

        JSONObject body = new JSONObject();
        body.put("exec", sql);
        return request.execute("POST", "/sql", body);
    }

    public Response info() {
        return request.execute("GET", "/");
    }

    public Response clusterHealth() {
        return request.execute("GET", "/cluster/health");
    }

    public Response clusterStats() {
        return request.execute("GET", "/cluster/stats");
    }

    public Response clusterNodes() {
        return request.execute("GET", "/cluster/nodes");
    }

    public Response links() {
        return request.execute("GET", "/links");
    }

    public Response linksPing() {
        return request.execute("GET", "/links/ping");
    }

    public Response linksConnect(String endpointOrHost, Integer port) {
        JSONObject body = new JSONObject();
        if (port != null) {
            body.put("host", endpointOrHost);
            body.put("port", port);
        } else {
            body.put("endpoint", endpointOrHost);
        }
        return request.execute("POST", "/links/connect", body);
    }

    public Response linksDisconnect(String endpointOrHost, Integer port) {
        JSONObject body = new JSONObject();
        if (port != null) {
            body.put("host", endpointOrHost);
            body.put("port", port);
        } else {
            body.put("endpoint", endpointOrHost);
        }
        return request.execute("POST", "/links/disconnect", body);
    }

    public Response flush() {
        return request.execute("POST", "/flush");
    }

    // API Instances
    public Collections collections() {
        return collections;
    }

    public Documents documents() {
        return documents;
    }

    public Search searchApi() {
        return search;
    }

    // Convenience Methods
    public Response listCollections(int offset, int limit) {
        return collections.list(offset, limit);
    }

    public Response listCollectionsDistributed() {
        return request.execute("GET", "/collections/distributed");
    }

    public Response getCollection(String name) {
        return collections.get(name);
    }

    public Response getCollectionFields(String name) {
        return collections.getFields(name);
    }

    public Response listDocuments(String collectionName, Map<String, Object> params) {
        return documents.list(collectionName, params);
    }

    public Response getDocument(String collectionName, String documentId) {
        return documents.get(collectionName, documentId);
    }

    public Response search(String collectionName, Map<String, Object> params) {
        return search.search(collectionName, params);
    }

    public Response samSearch(String collectionName, String query) {
        return samSearch(collectionName, query, new HashMap<>());
    }

    public Response samSearch(String collectionName, String query, Map<String, Object> params) {
        return search.samSearch(collectionName, query, params);
    }

    public Response samSearchAll(String query) {
        return samSearchAll(query, new HashMap<>());
    }

    public Response samSearchAll(String query, Map<String, Object> params) {
        return search.samSearchAll(query, params);
    }

    public Response sqlSearch(String collectionName, String sql) {
        return sqlSearch(collectionName, sql, new HashMap<>());
    }

    public Response sqlSearch(String collectionName, String sql, Map<String, Object> params) {
        return search.sql(collectionName, sql, params);
    }

    public Response vectorSearch(String collectionName, Map<String, Object> params) {
        return search.vectorSearch(collectionName, params);
    }

    public Response executeRequest(String method, String path, Object body, Map<String, String> queryParams) {
        return request.execute(method, path, body, queryParams);
    }

    public Response executeRequestSafe(String method, String path, Object body, Map<String, String> queryParams) {
        return request.executeSafe(method, path, body, queryParams);
    }

    public Response executeRequestWithRetry(
            String method,
            String path,
            Object body,
            Map<String, String> queryParams,
            int maxRetries,
            long initialBackoffMillis
    ) {
        return request.executeWithRetry(method, path, body, queryParams, maxRetries, initialBackoffMillis);
    }
}
