package hlquery;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Example {
    public static void main(String[] args) {
        String baseUrl = "http://localhost:9200";
        Client client = new Client(baseUrl);

        System.out.println("=== hlquery Java API Example ===");

        try {
            // 1. Health check
            System.out.println("\n1. Running health check...");
            Response healthResp = client.health();
            if (healthResp.isSuccess()) {
                System.out.println("   ✓ Health check successful: " + healthResp.getRawBody());
            } else {
                System.out.println("   ✗ Health check failed: " + healthResp.getStatusCode());
            }

            // 2. Create collection
            String collectionName = "test_java_" + System.currentTimeMillis();
            System.out.println("\n2. Creating collection '" + collectionName + "'...");
            
            JSONObject schema = new JSONObject();
            JSONArray fields = new JSONArray();
            fields.put(new JSONObject().put("name", "title").put("type", "string"));
            fields.put(new JSONObject().put("name", "content").put("type", "string"));
            fields.put(new JSONObject().put("name", "value").put("type", "int"));
            schema.put("fields", fields);
            
            Response createResp = client.collections().create(collectionName, schema);
            if (createResp.isSuccess()) {
                System.out.println("   ✓ Collection created successfully");
            } else {
                System.out.println("   ✗ Failed to create collection: " + createResp.getStatusCode());
                System.out.println("     Error: " + createResp.getRawBody());
            }

            // 3. Add document
            System.out.println("\n3. Adding a document...");
            JSONObject doc = new JSONObject();
            doc.put("id", "doc_1");
            doc.put("title", "Hello Java");
            doc.put("content", "This is a document from the Java API client");
            doc.put("value", 100);
            
            Response addResp = client.documents().add(collectionName, doc);
            if (addResp.isSuccess()) {
                System.out.println("   ✓ Document added successfully");
            } else {
                System.out.println("   ✗ Failed to add document: " + addResp.getStatusCode());
            }

            // 4. Search
            System.out.println("\n4. Searching for 'Java'...");
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("q", "Java");
            searchParams.put("query_by", "title,content");
            
            Response searchResp = client.search(collectionName, searchParams);
            if (searchResp.isSuccess()) {
                System.out.println("   ✓ Search results: " + searchResp.getRawBody());
            } else {
                System.out.println("   ✗ Search failed: " + searchResp.getStatusCode());
            }

            // 5. List all collections
            System.out.println("\n5. Listing all collections...");
            Response listResp = client.listCollections(0, 100);
            if (listResp.isSuccess()) {
                JSONObject listBody = listResp.getBodyAsObject();
                JSONArray collections = listBody.optJSONArray("collections");
                if (collections != null) {
                    System.out.println("   ✓ Found " + collections.length() + " collections:");
                    for (int i = 0; i < collections.length(); i++) {
                        System.out.println("     - " + collections.get(i));
                    }
                }
            }

            // 6. Cleanup
            System.out.println("\n6. Deleting collection...");
            Response deleteResp = client.collections().delete(collectionName);
            if (deleteResp.isSuccess()) {
                System.out.println("   ✓ Collection deleted");
            } else {
                System.out.println("   ✗ Failed to delete collection: " + deleteResp.getStatusCode());
                System.out.println("     Error: " + deleteResp.getRawBody());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
