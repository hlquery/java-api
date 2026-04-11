package hlquery.examples;

import hlquery.Client;
import hlquery.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Search {
    public static void main(String[] args) {
        Client client = new Client("http://localhost:9200");
        String collectionName = "java_search_demo";

        System.out.println("🧪 Search API Examples");

        // Setup
        JSONObject schema = new JSONObject();
        schema.put("fields", new JSONArray()
                .put(new JSONObject().put("name", "title").put("type", "string"))
                .put(new JSONObject().put("name", "content").put("type", "string"))
                .put(new JSONObject().put("name", "category").put("type", "string"))
                .put(new JSONObject().put("name", "price").put("type", "int32")));
        client.collections().create(collectionName, schema);
        client.documents().add(collectionName, new JSONObject().put("id", "1").put("title", "Wireless Keyboard").put("content", "Compact laptop accessory").put("category", "electronics").put("price", 120));
        client.documents().add(collectionName, new JSONObject().put("id", "2").put("title", "Refurbished Laptop").put("content", "Budget notebook option").put("category", "electronics").put("price", 80));

        // 1. Basic search
        System.out.println("\n1. Basic search for 'keyboard'...");
        Map<String, Object> params = new HashMap<>();
        params.put("q", "keyboard");
        params.put("query_by", "title,content");
        Response res = client.search(collectionName, params);
        if (res.isSuccess()) {
            System.out.println("   ✓ Hits: " + res.getBodyAsObject().optJSONArray("hits").length());
        }

        // 2. Fielded / boolean search
        System.out.println("\n2. Fielded / boolean search...");
        Map<String, Object> fieldParams = new HashMap<>();
        fieldParams.put("q", "title:keyboard OR title:laptop");
        fieldParams.put("query_by", "title,content");
        Response fieldRes = client.search(collectionName, fieldParams);
        if (fieldRes.isSuccess()) {
            System.out.println("   ✓ Hits: " + fieldRes.getBodyAsObject().optJSONArray("hits").length());
        }

        // 3. filter_by operator search
        System.out.println("\n3. filter_by operator search...");
        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("q", "*");
        filterParams.put("query_by", "title,content");
        filterParams.put("filter_by", "price:>100&&category:electronics");
        Response filterRes = client.search(collectionName, filterParams);
        if (filterRes.isSuccess()) {
            System.out.println("   ✓ Hits: " + filterRes.getBodyAsObject().optJSONArray("hits").length());
        }

        // 4. Vector search
        System.out.println("\n4. Vector search demo...");
        Map<String, Object> vectorParams = new HashMap<>();
        Map<String, Object> vectorBody = new HashMap<>();
        vectorBody.put("vector", new float[]{0.1f, 0.2f, 0.3f});
        vectorBody.put("field_name", "embedding");
        vectorBody.put("topk", 5);
        vectorBody.put("include_distance", true);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("ef", 64);
        queryParams.put("nprobe", 4);
        queryParams.put("is_linear", true);
        vectorBody.put("query_params", queryParams);
        vectorParams.put("body", vectorBody);
        Response vecRes = client.vectorSearch(collectionName, vectorParams);
        if (vecRes.getStatusCode() != 404) {
            System.out.println("   ✓ Vector search executed (Status: " + vecRes.getStatusCode() + ")");
        }

        // Cleanup
        client.collections().delete(collectionName);
    }
}
