package hlquery.examples;

import hlquery.Client;
import hlquery.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class SQL {
    public static void main(String[] args) {
        Client client = new Client("http://localhost:9200");
        String collectionName = "java_sql_demo";

        System.out.println("SQL API Example");

        client.collections().delete(collectionName);

        JSONObject schema = new JSONObject();
        schema.put("fields", new JSONArray()
                .put(new JSONObject().put("name", "title").put("type", "string"))
                .put(new JSONObject().put("name", "category").put("type", "string"))
                .put(new JSONObject().put("name", "price").put("type", "int32")));

        Response create = client.collections().create(collectionName, schema);
        System.out.println("Create collection: " + create.getStatusCode());

        client.documents().add(collectionName, new JSONObject()
                .put("id", "sku_1")
                .put("title", "Trail Running Shoes")
                .put("category", "footwear")
                .put("price", 129));

        client.documents().add(collectionName, new JSONObject()
                .put("id", "sku_2")
                .put("title", "Waterproof Jacket")
                .put("category", "outerwear")
                .put("price", 189));

        Response select = client.sqlSearch(
                collectionName,
                "SELECT id, title, price FROM " + collectionName + " ORDER BY price DESC LIMIT 5;"
        );
        System.out.println("Collection SQL SELECT:");
        System.out.println(select.getRawBody());

        Response showCollections = client.sql("SHOW COLLECTIONS;");
        System.out.println("SHOW COLLECTIONS:");
        System.out.println(showCollections.getRawBody());

        Response cleanup = client.collections().delete(collectionName);
        System.out.println("Cleanup: " + cleanup.getStatusCode());
    }
}
