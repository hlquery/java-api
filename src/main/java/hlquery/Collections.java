package hlquery;

import hlquery.utils.Validator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Collections {
    private final Request request;

    public Collections(Request request) {
        this.request = request;
    }

    public Response list(int offset, int limit) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("offset", String.valueOf(offset));
        queryParams.put("limit", String.valueOf(limit));
        return request.execute("GET", "/collections", null, queryParams);
    }

    public Response get(String name) {
        Validator.validateCollectionName(name);
        return request.execute("GET", "/collections/" + URLEncoder.encode(name, StandardCharsets.UTF_8));
    }

    public Response create(String name, JSONObject schema) {
        Validator.validateCollectionName(name);

        JSONObject body = schema != null ? new JSONObject(schema.toString()) : new JSONObject();
        body.put("name", name);

        return request.execute("POST", "/collections", body);
    }

    public Response delete(String name) {
        Validator.validateCollectionName(name);
        return request.execute("DELETE", "/collections/" + URLEncoder.encode(name, StandardCharsets.UTF_8));
    }

    public Response update(String name, JSONObject schema) {
        Validator.validateCollectionName(name);
        return request.execute("POST", "/collections/" + URLEncoder.encode(name, StandardCharsets.UTF_8) + "/update", schema);
    }

    public Response getFields(String name) {
        Response response = get(name);
        if (response.getStatusCode() != 200) {
            return response;
        }

        JSONObject body = response.getBodyAsObject();
        Map<String, List<String>> fieldTypes = new HashMap<>();
        List<String> allFields = new ArrayList<>();

        collectFields(body, "searchable_fields", "searchable", allFields, fieldTypes);
        collectFields(body, "filterable_fields", "filterable", allFields, fieldTypes);
        collectFields(body, "sortable_fields", "sortable", allFields, fieldTypes);

        JSONArray fieldsArray = new JSONArray();
        for (String field : allFields) {
            JSONObject fieldObj = new JSONObject();
            fieldObj.put("name", field);
            fieldObj.put("type", String.join(", ", fieldTypes.get(field)));
            fieldsArray.put(fieldObj);
        }

        JSONObject result = new JSONObject();
        result.put("collection", name);
        result.put("fields", fieldsArray);
        result.put("field_count", fieldsArray.length());
        JSONArray searchable = body.optJSONArray("searchable_fields");
        JSONArray filterable = body.optJSONArray("filterable_fields");
        JSONArray sortable = body.optJSONArray("sortable_fields");
        result.put("searchable_fields", searchable != null ? searchable : new JSONArray());
        result.put("filterable_fields", filterable != null ? filterable : new JSONArray());
        result.put("sortable_fields", sortable != null ? sortable : new JSONArray());

        return new Response(200, result.toString(), response.getHeaders());
    }

    public Response getLanguage(String name) {
        Validator.validateCollectionName(name);
        return request.execute("GET", "/collections/" + URLEncoder.encode(name, StandardCharsets.UTF_8) + "/lang");
    }

    private void collectFields(JSONObject body, String key, String type, List<String> allFields, Map<String, List<String>> fieldTypes) {
        if (body.has(key)) {
            JSONArray fields = body.getJSONArray(key);
            for (int i = 0; i < fields.length(); i++) {
                String field = fields.getString(i);
                if (!allFields.contains(field)) {
                    allFields.add(field);
                }
                fieldTypes.computeIfAbsent(field, k -> new ArrayList<>()).add(type);
            }
        }
    }
}
