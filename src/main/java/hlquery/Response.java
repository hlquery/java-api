package hlquery;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.List;
import java.util.Map;

public class Response {
    private final int statusCode;
    private final String body;
    private final Map<String, List<String>> headers;

    public Response(int statusCode, String body, Map<String, List<String>> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRawBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public JSONObject getBodyAsObject() {
        try {
            return body != null ? new JSONObject(body) : new JSONObject();
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public JSONArray getBodyAsArray() {
        try {
            return body != null ? new JSONArray(body) : new JSONArray();
        } catch (JSONException e) {
            return new JSONArray();
        }
    }
    
    public Object getBody() {
        if (body == null || body.isEmpty()) return null;
        try {
            if (body.trim().startsWith("[")) {
                return new JSONArray(body);
            } else {
                return new JSONObject(body);
            }
        } catch (JSONException e) {
            return body;
        }
    }
}
