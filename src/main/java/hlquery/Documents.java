package hlquery;

import hlquery.utils.Validator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Documents {
    private final Request request;

    public Documents(Request request) {
        this.request = request;
    }

    public Response list(String collectionName, Map<String, Object> params) {
        Validator.validateCollectionName(collectionName);
        
        Map<String, String> queryParams = new HashMap<>();
        if (params != null) {
            Object offset = params.getOrDefault("offset", params.getOrDefault("from", 0));
            Object limit = params.getOrDefault("limit", params.getOrDefault("size", 10));
            queryParams.put("offset", String.valueOf(offset));
            queryParams.put("limit", String.valueOf(limit));
        } else {
            queryParams.put("offset", "0");
            queryParams.put("limit", "10");
        }
        
        return request.execute("GET", "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents", null, queryParams);
    }

    public Response get(String collectionName, String documentId) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(documentId);
        
        return request.execute("GET", "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/" + URLEncoder.encode(documentId, StandardCharsets.UTF_8));
    }

    public Response add(String collectionName, JSONObject document) {
        Validator.validateCollectionName(collectionName);
        return request.execute("POST", "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents", document);
    }

    public Response update(String collectionName, String documentId, JSONObject document) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(documentId);
        return request.execute("PUT", "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/" + URLEncoder.encode(documentId, StandardCharsets.UTF_8), document);
    }

    public Response delete(String collectionName, String documentId) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(documentId);
        return request.execute("DELETE", "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/" + URLEncoder.encode(documentId, StandardCharsets.UTF_8));
    }

    public Response importDocuments(String collectionName, JSONArray documents) {
        Validator.validateCollectionName(collectionName);
        JSONObject body = new JSONObject();
        body.put("documents", documents);
        return request.execute("POST", "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/import", body);
    }

    public Response facetCounts(String collectionName, Map<String, Object> params) {
        return collectionDocumentQuery(collectionName, "facet_counts", params);
    }

    public Response maybe(String collectionName, Map<String, Object> params) {
        return collectionDocumentQuery(collectionName, "maybe", params);
    }

    public Response export(String collectionName, Map<String, Object> params) {
        return collectionDocumentQuery(collectionName, "export", params);
    }

    public Response context(String collectionName, String documentId, Map<String, Object> params) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(documentId);
        return request.execute(
                "GET",
                "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8)
                        + "/documents/" + URLEncoder.encode(documentId, StandardCharsets.UTF_8) + "/context",
                null,
                toQueryParams(params)
        );
    }

    public Response deleteByFilter(String collectionName, String filter) {
        Validator.validateCollectionName(collectionName);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("filter_by", filter);
        return request.execute("DELETE", "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents", null, queryParams);
    }

    public Response deleteAll(String collectionName) {
        Validator.validateCollectionName(collectionName);
        return request.execute("DELETE", "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents");
    }

    public Response updateByQuery(String collectionName, JSONObject body) {
        Validator.validateCollectionName(collectionName);
        return request.execute(
                "POST",
                "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/_update_by_query",
                body != null ? body : new JSONObject()
        );
    }

    public Response deleteByQuery(String collectionName, JSONObject body) {
        Validator.validateCollectionName(collectionName);
        return request.execute(
                "POST",
                "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/_delete_by_query",
                body != null ? body : new JSONObject()
        );
    }

    private Response collectionDocumentQuery(String collectionName, String route, Map<String, Object> params) {
        Validator.validateCollectionName(collectionName);
        boolean post = params != null && params.containsKey("body");
        Object body = post ? params.get("body") : null;
        return request.execute(
                post ? "POST" : "GET",
                "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/" + route,
                body,
                post ? null : toQueryParams(params)
        );
    }

    private Map<String, String> toQueryParams(Map<String, Object> params) {
        Map<String, String> queryParams = new HashMap<>();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    queryParams.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        return queryParams;
    }
}
