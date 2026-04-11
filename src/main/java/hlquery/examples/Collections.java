package hlquery.examples;

import hlquery.Client;
import hlquery.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Collections {
    public static void main(String[] args) {
        Client client = new Client("http://localhost:9200");
        String collectionName = "java_collections_demo";

        System.out.println("🧪 Collections API Examples");

        // 1. List collections
        System.out.println("\n1. Listing collections...");
        Response listRes = client.listCollections(0, 10);
        if (listRes.isSuccess()) {
            System.out.println("   ✓ Found collections: " + listRes.getBodyAsObject().optJSONArray("collections"));
        }

        // 2. Create a collection
        System.out.println("\n2. Creating collection '" + collectionName + "'...");
        JSONObject schema = new JSONObject();
        JSONArray fields = new JSONArray();
        fields.put(new JSONObject().put("name", "title").put("type", "string"));
        fields.put(new JSONObject().put("name", "price").put("type", "float"));
        schema.put("fields", fields);

        Response createRes = client.collections().create(collectionName, schema);
        if (createRes.isSuccess()) {
            System.out.println("   ✓ Created successfully");
        } else {
            System.out.println("   ✗ Creation failed: " + createRes.getRawBody());
        }

        // 3. Get collection details
        System.out.println("\n3. Getting collection details...");
        Response getRes = client.collections().get(collectionName);
        if (getRes.isSuccess()) {
            System.out.println("   ✓ Details: " + getRes.getRawBody());
        }

        // 4. Update collection
        System.out.println("\n4. Updating collection schema...");
        fields.put(new JSONObject().put("name", "description").put("type", "string"));
        Response updateRes = client.collections().update(collectionName, schema);
        if (updateRes.isSuccess()) {
            System.out.println("   ✓ Updated successfully");
        }

        // 5. Cleanup
        System.out.println("\n5. Deleting collection...");
        client.collections().delete(collectionName);
        System.out.println("   ✓ Deleted");
    }
}
