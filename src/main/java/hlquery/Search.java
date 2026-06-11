package hlquery;

import hlquery.utils.Validator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Search {
    private final Request request;
    private final Collections collections;

    public Search(Request request, Collections collections) {
        this.request = request;
        this.collections = collections;
    }

    public Response sql(String collectionName, String sql, Map<String, Object> params) {
        Validator.validateCollectionName(collectionName);

        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query must be a non-empty string");
        }

        Map<String, String> queryParams = new HashMap<>();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    queryParams.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        queryParams.put("sql", sql);

        return request.execute(
                "GET",
                "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/search",
                null,
                queryParams
        );
    }

    public Response sqlPost(String collectionName, String sql, Map<String, Object> params) {
        Validator.validateCollectionName(collectionName);

        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query must be a non-empty string");
        }

        JSONObject body = new JSONObject();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    body.put(entry.getKey(), entry.getValue());
                }
            }
        }
        body.put("sql", sql);

        return request.execute(
                "POST",
                "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/search",
                body
        );
    }

    public Response search(String collectionName, Map<String, Object> params) {
        Validator.validateCollectionName(collectionName);
        
        Map<String, String> queryParams = new HashMap<>();
        
        if (params != null) {
            // Handle structured query object (Elasticsearch-like)
            if (params.get("query") instanceof Map) {
                Map<String, Object> query = (Map<String, Object>) params.get("query");
                if (query.get("q") != null) {
                    queryParams.put("q", String.valueOf(query.get("q")));
                }
                if (query.get("query_by") != null) {
                    queryParams.put("query_by", formatCommaSeparated(query.get("query_by")));
                }
            }
            
            // Direct query parameters
            if (params.get("q") != null) {
                queryParams.put("q", String.valueOf(params.get("q")));
            }
            
            if (params.get("query_by") != null) {
                queryParams.put("query_by", formatCommaSeparated(params.get("query_by")));
            } else if (queryParams.get("q") != null && !queryParams.get("q").isEmpty()) {
                // Auto-detect searchable fields
                Response collectionResp = collections.get(collectionName);
                if (collectionResp.getStatusCode() == 200) {
                    JSONObject body = collectionResp.getBodyAsObject();
                    if (body.has("searchable_fields")) {
                        queryParams.put("query_by", formatCommaSeparated(body.get("searchable_fields")));
                    }
                }
            }
            
            // Pagination
            if (params.containsKey("from")) queryParams.put("offset", String.valueOf(params.get("from")));
            if (params.containsKey("offset")) queryParams.put("offset", String.valueOf(params.get("offset")));
            if (params.containsKey("size")) queryParams.put("limit", String.valueOf(params.get("size")));
            if (params.containsKey("limit")) queryParams.put("limit", String.valueOf(params.get("limit")));
            if (params.containsKey("page")) queryParams.put("page", String.valueOf(params.get("page")));
            if (params.containsKey("per_page")) queryParams.put("per_page", String.valueOf(params.get("per_page")));
            
            // Filters
            if (params.containsKey("filter_by")) {
                queryParams.put("filter_by", String.valueOf(params.get("filter_by")));
            } else if (params.containsKey("filter")) {
                Object filter = params.get("filter");
                queryParams.put("filter_by", filter instanceof Map ? new JSONObject((Map) filter).toString() : String.valueOf(filter));
            }
            
            // Sort
            if (params.containsKey("sort")) {
                queryParams.put("sort_by", formatSort(params.get("sort")));
            } else if (params.containsKey("sort_by")) {
                queryParams.put("sort_by", formatCommaSeparated(params.get("sort_by")));
            }
            
            // Facets
            if (params.containsKey("facet_by")) queryParams.put("facet_by", formatCommaSeparated(params.get("facet_by")));
            else if (params.containsKey("facets")) queryParams.put("facet_by", formatCommaSeparated(params.get("facets")));
            
            // typos
            if (params.containsKey("typo_tolerance")) queryParams.put("typo_tolerance", String.valueOf(params.get("typo_tolerance")));
            if (params.containsKey("num_typos")) queryParams.put("num_typos", String.valueOf(params.get("num_typos")));
            
            // Highlighting
            if (params.containsKey("highlight")) queryParams.put("highlight", String.valueOf(params.get("highlight")));
            if (params.containsKey("highlight_fields")) queryParams.put("highlight_fields", formatCommaSeparated(params.get("highlight_fields")));
            if (params.containsKey("highlight_full_fields")) queryParams.put("highlight_full_fields", formatCommaSeparated(params.get("highlight_full_fields")));
        }
        
        String method = (params != null && params.containsKey("body")) ? "POST" : "GET";
        Object body = (params != null) ? params.get("body") : null;
        
        return request.execute(method, "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/documents/search", body, queryParams);
    }

    public Response multiSearch(JSONArray searches) {
        JSONObject body = new JSONObject();
        body.put("searches", searches);
        return request.execute("POST", "/multi_search", body);
    }

    public Response multiSearch(JSONObject body) {
        return request.execute("POST", "/multi_search", body != null ? body : new JSONObject());
    }

    public Response globalSearch(Map<String, Object> params) {
        boolean post = params != null && params.containsKey("body");
        Object body = post ? params.get("body") : null;
        return request.execute(post ? "POST" : "GET", "/search", body, post ? null : toQueryParams(params));
    }

    public Response vectorSearch(String collectionName, Map<String, Object> params) {
        Validator.validateCollectionName(collectionName);
        Map<String, String> queryParams = new HashMap<>();
        boolean forcePost = false;
        
        if (params != null) {
            Object vector = params.get("vector_query");
            if (vector == null) vector = params.get("vector");
            if (vector == null) vector = params.get("embedding");
            if (vector != null) {
                if (vector instanceof List || vector instanceof double[] || vector instanceof float[]) {
                    queryParams.put("vector_query", new JSONArray(vector).toString());
                } else {
                    queryParams.put("vector_query", String.valueOf(vector));
                }
            }
            
            Object fieldName = params.get("field_name");
            if (fieldName == null) fieldName = params.get("field");
            if (fieldName == null) fieldName = params.get("fieldName");
            if (fieldName != null) queryParams.put("field_name", String.valueOf(fieldName));

            if (params.containsKey("limit")) queryParams.put("limit", String.valueOf(params.get("limit")));
            else if (params.containsKey("topk")) queryParams.put("limit", String.valueOf(params.get("topk")));
            else if (params.containsKey("top_k")) queryParams.put("limit", String.valueOf(params.get("top_k")));
            else if (params.containsKey("topK")) queryParams.put("limit", String.valueOf(params.get("topK")));
            else if (params.containsKey("k")) queryParams.put("limit", String.valueOf(params.get("k")));
            else if (params.containsKey("per_page")) queryParams.put("limit", String.valueOf(params.get("per_page")));

            if (params.containsKey("threshold")) queryParams.put("threshold", String.valueOf(params.get("threshold")));
            if (params.containsKey("normalize")) queryParams.put("normalize", String.valueOf(params.get("normalize")));

            if (params.containsKey("output_fields")) queryParams.put("output_fields", formatCommaSeparated(params.get("output_fields")));
            else if (params.containsKey("outputFields")) queryParams.put("output_fields", formatCommaSeparated(params.get("outputFields")));

            if (params.containsKey("include_vector")) queryParams.put("include_vector", String.valueOf(params.get("include_vector")));
            else if (params.containsKey("includeVector")) queryParams.put("include_vector", String.valueOf(params.get("includeVector")));

            if (params.containsKey("include_distance")) queryParams.put("include_distance", String.valueOf(params.get("include_distance")));
            else if (params.containsKey("includeDistance")) queryParams.put("include_distance", String.valueOf(params.get("includeDistance")));

            if (params.containsKey("filter_by")) queryParams.put("filter_by", String.valueOf(params.get("filter_by")));
            else if (params.containsKey("filter")) queryParams.put("filter_by", String.valueOf(params.get("filter")));

            if (params.containsKey("radius")) queryParams.put("radius", String.valueOf(params.get("radius")));
            if (params.containsKey("max_distance")) queryParams.put("max_distance", String.valueOf(params.get("max_distance")));
            if (params.containsKey("range_filter")) queryParams.put("range_filter", String.valueOf(params.get("range_filter")));
            if (params.containsKey("min_distance")) queryParams.put("min_distance", String.valueOf(params.get("min_distance")));

            if (params.containsKey("query_params")) {
                queryParams.put("query_params", String.valueOf(params.get("query_params")));
                forcePost = true;
            } else if (params.containsKey("queryParams")) {
                queryParams.put("query_params", String.valueOf(params.get("queryParams")));
                forcePost = true;
            } else if (params.containsKey("params")) {
                queryParams.put("params", String.valueOf(params.get("params")));
                forcePost = true;
            }

            if (params.containsKey("vector_queries") || params.containsKey("vectorQueries")) {
                forcePost = true;
            }
        }
        
        String method = (params != null && params.containsKey("body")) || forcePost ? "POST" : "GET";
        Object body = (params != null) ? params.get("body") : null;
        if (body == null && forcePost) {
            body = new JSONObject(params);
            queryParams.clear();
        }
        
        return request.execute(method, "/collections/" + URLEncoder.encode(collectionName, StandardCharsets.UTF_8) + "/vector_search", body, queryParams);
    }

    private String formatCommaSeparated(Object obj) {
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            String[] parts = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                parts[i] = String.valueOf(list.get(i));
            }
            return String.join(",", parts);
        } else if (obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) obj;
            String[] parts = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) parts[i] = arr.getString(i);
            return String.join(",", parts);
        }
        return String.valueOf(obj);
    }

    private String formatSort(Object sort) {
        if (sort instanceof List) {
            List<?> list = (List<?>) sort;
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                if (sb.length() > 0) sb.append(",");
                if (item instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) item;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        String key = String.valueOf(entry.getKey());
                        String value = String.valueOf(entry.getValue());
                        sb.append("desc".equalsIgnoreCase(value) ? "-" : "").append(key);
                    }
                } else {
                    sb.append(item);
                }
            }
            return sb.toString();
        }
        return String.valueOf(sort);
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
