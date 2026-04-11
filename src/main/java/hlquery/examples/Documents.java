package hlquery.examples;

import hlquery.Client;
import hlquery.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Documents {
    public static void main(String[] args) {
        Client client = new Client("http://localhost:9200");
        String collectionName = "java_docs_demo";

        System.out.println("🧪 Documents API Examples");

        // Setup: Create collection
        JSONObject schema = new JSONObject();
        JSONArray fields = new JSONArray();
        fields.put(new JSONObject().put("name", "title").put("type", "string"));
        schema.put("fields", fields);
        client.collections().create(collectionName, schema);

        // 1. Add a document
        System.out.println("\n1. Adding a document...");
        JSONObject doc = new JSONObject();
        doc.put("id", "doc1");
        doc.put("title", "First Java Document");
        Response addRes = client.documents().add(collectionName, doc);
        if (addRes.isSuccess()) {
            System.out.println("   ✓ Added successfully");
        }

        // 2. Get document
        System.out.println("\n2. Getting document 'doc1'...");
        Response getRes = client.documents().get(collectionName, "doc1");
        if (getRes.isSuccess()) {
            System.out.println("   ✓ Found: " + getRes.getRawBody());
        }

        // 3. Update document
        System.out.println("\n3. Updating document 'doc1'...");
        JSONObject update = new JSONObject().put("title", "Updated Java Document");
        Response updateRes = client.documents().update(collectionName, "doc1", update);
        if (updateRes.isSuccess()) {
            System.out.println("   ✓ Updated successfully");
        }

        // 4. Import multiple documents
        System.out.println("\n4. Bulk importing documents...");
        JSONArray docs = new JSONArray();
        docs.put(new JSONObject().put("id", "doc2").put("title", "Second Doc"));
        docs.put(new JSONObject().put("id", "doc3").put("title", "Third Doc"));
        Response importRes = client.documents().importDocuments(collectionName, docs);
        if (importRes.isSuccess()) {
            System.out.println("   ✓ Imported " + docs.length() + " documents");
        }

        // 5. List documents
        System.out.println("\n5. Listing documents...");
        Response listRes = client.documents().list(collectionName, new HashMap<>());
        if (listRes.isSuccess()) {
            System.out.println("   ✓ Documents in collection: " + listRes.getBodyAsObject().optJSONArray("documents").length());
        }

        // Cleanup
        client.collections().delete(collectionName);
    }
}
