<div align="center">
  <img src="../../docs/static/img/hlquery/2.png" alt="hlquery logo" width="200">
</div>

<div align="center">

**A clean, idiomatic Java client library for hlquery, designed with a familiar and intuitive API structure.**

[![Follow hlquery](https://img.shields.io/badge/Follow-%40hlquery-blue?logo=x&logoColor=white&labelColor=000000)](https://x.com/hlquery)
[![Java build](https://img.shields.io/badge/Java%20build-passing-brightgreen?logo=openjdk&logoColor=white&labelColor=000000)](https://github.com/hlquery/java-api/actions/workflows/java-api.yml)
[![java-api](https://img.shields.io/badge/GitHub-java--api-purple?logo=github&logoColor=white&labelColor=000000)](https://github.com/hlquery/java-api/)
[![hlquery](https://img.shields.io/badge/GitHub-hlquery-blue?logo=github&logoColor=white&labelColor=000000)](https://github.com/hlquery/hlquery/)
[![License](https://img.shields.io/badge/License-BSD%203--Clause-a35a0f?logo=open-source-initiative&logoColor=white&labelColor=000000)](https://opensource.org/licenses/BSD-3-Clause)

</div>

### What is the hlquery Java API?

The hlquery Java API is the official Java client for [hlquery](https://github.com/hlquery/hlquery). It wraps hlquery's HTTP/JSON endpoints in a single client object with helpers for collections, documents, search, vector search, and SQL.

It is intended for JVM services, tools, and applications that want a straightforward hlquery integration layer instead of manual request construction.

### Why use it?

The Java API gives Java applications a familiar client layout, response objects for status checks and raw body access, coverage for the main hlquery endpoint families, and a simple path for both high-level helpers and raw requests.

### Install

Build with the included `Makefile`:

```bash
$ make
```

This downloads the required `org.json` dependency and compiles the client sources.

### SQL

```java
Client client = new Client(Config.getDefaultBaseUrl());

Response rows = client.sql("SHOW COLLECTIONS;");
Response products = client.sqlSearch(
    "products",
    "SELECT id, title, price FROM products ORDER BY price DESC LIMIT 3;"
);
Response insert = client.sqlWrite(
    "INSERT INTO products (id, title, price) VALUES ('sku-4', 'Desk Lamp', 49);"
);
Response delete = client.sqlExec("DELETE FROM products WHERE id = 'sku-4';");
Response drop = client.sqlPost("DROP COLLECTION old_products;");

System.out.println(rows.getRawBody());
System.out.println(products.getRawBody());
```

The SQL helpers cover top-level SQL (`client.sql`, `client.sqlPost`, `client.sqlExec`, `client.sqlSelect`, `client.sqlWrite`) and collection-bound SQL search (`client.sqlSearch`, `client.sqlSearchPost`). Use top-level SQL for `SHOW COLLECTIONS`, `INSERT`, `DELETE`, and `DROP`; use collection-bound SQL for `SELECT` queries tied to one collection.

### Reduce Text Example

Use the raw request helper for custom module routes:

```java
Map<String, String> query = new HashMap<>();
query.put("q", "example query");

Response moduleResponse = client.executeRequest(
    "GET",
    "/modules/<name>/<route>",
    null,
    query
);

System.out.println(moduleResponse.getRawBody());
```

### TODO

- Add a small typed layer (request/response DTOs) for the most common endpoints.
- Publish as a proper Maven artifact (use Maven/Gradle dependency management, not `curl`).

### Contributing

We welcome contributions from the community! All contributions must be released under the BSD 3-Clause license.

### How to Contribute

- Check existing [Java API issues](https://github.com/hlquery/java-api/issues) or create new ones
- Contribute Java client changes to [hlquery/java-api](https://github.com/hlquery/java-api)
- Contribute shared server/API changes to [hlquery/hlquery](https://github.com/hlquery/hlquery)
- Test and report bugs against the Java client
- Improve Java-specific documentation and examples

### Community

- [Documentation](https://docs.hlquery.com)
- [X (Twitter)](https://x.com/hlquery)
- [Java API GitHub](https://github.com/hlquery/java-api)
- [hlquery GitHub](https://github.com/hlquery/hlquery)

### License

The hlquery Java API is licensed under the [BSD 3-Clause License](https://opensource.org/licenses/BSD-3-Clause).
