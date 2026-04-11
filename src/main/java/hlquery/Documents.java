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

    public Response deleteByFilter(String collectionName, String filter) {
        Validator.validateCollectionName(collectionName);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("filter_by", filter);
        return request.execute("DELETE", "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents", null, queryParams);
    }
}
