<div align="center">
  <img src="https://docs.hlquery.com/img/hlquery/2.png" alt="hlquery logo" width="200">
</div>

<div align="center">

**A clean, idiomatic Java client library for hlquery, designed with a familiar and intuitive API structure.**

[![Follow hlquery](https://img.shields.io/badge/Follow-%40hlquery-blue?logo=x&logoColor=white)](https://x.com/hlquery)
[![Commit Activity](https://img.shields.io/github/commit-activity/m/hlquery/java-api)](https://github.com/hlquery/java-api/pulse)
[![GitHub](https://img.shields.io/badge/GitHub-java--api-181717?logo=github&logoColor=white)](https://github.com/hlquery/java-api/stargazers)
[![hlquery](https://img.shields.io/badge/GitHub-hlquery-blue?logo=github&logoColor=white)](https://github.com/hlquery/hlquery/stargazers)
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

</div>

### What is the hlquery Java API?

The hlquery Java API is the official Java client for hlquery. It wraps hlquery's HTTP/JSON endpoints in a single client object with helpers for collections, documents, search, vector search, SQL, and SAM.

It is intended for JVM services, tools, and applications that want a straightforward hlquery integration layer instead of manual request construction.

### Why use it?

- Familiar client layout for Java applications.
- Response objects for status checks and raw body access.
- Coverage for the main hlquery endpoint families.
- Simple path for both high-level helpers and raw requests.

### Why choose it over raw HTTP?

- Less repetitive URL, auth, and request-body code.
- One client entry point for day-to-day hlquery tasks.
- Easier to keep request formatting and error handling consistent.

### Install

Build with the included `Makefile`:

```bash
make
```

This downloads the required `org.json` dependency and compiles the client sources.

### Quick Start

```java
import hlquery.Client;
import hlquery.Response;
import hlquery.utils.Config;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Client client = new Client(Config.getDefaultBaseUrl());

        Response health = client.health();
        System.out.println(health.getRawBody());

        Map<String, Object> params = new HashMap<>();
        params.put("q", "search term");
        params.put("query_by", "title,content");

        Response results = client.search("my_collection", params);
        System.out.println(results.getRawBody());
    }
}
```

### Auth

```java
Client client = new Client("http://localhost:9200");

client.setAuthToken("your_token_here", "bearer");
client.setAuthToken("your_api_key_here", "api-key");
```

### SAM

SAM is separate from vector search. It performs term and intent-style lookup, not vector similarity search.

```java
Client client = new Client(Config.getDefaultBaseUrl());

Response status = client.samStatus("music");
Response history = client.samHistory("music", 5);
Response results = client.samSearch("music", "queen of pop");

System.out.println(status.getRawBody());
System.out.println(history.getRawBody());
System.out.println(results.getRawBody());
```

### SQL

```java
Client client = new Client(Config.getDefaultBaseUrl());

Response rows = client.sql("SHOW COLLECTIONS;");
Response products = client.sqlSearch(
    "products",
    "SELECT id, title, price FROM products ORDER BY price DESC LIMIT 3;"
);

System.out.println(rows.getRawBody());
System.out.println(products.getRawBody());
```

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

### Running Examples

With `make`:

```bash
make example
make examples-search
make examples-sql
```

Manual execution:

```bash
mkdir -p bin
javac -cp lib/json.jar -d bin $(find src/main/java -name "*.java")
java -cp lib/json.jar:bin hlquery.Example
```

### Notes

- See `src/main/java/hlquery/examples/` for focused examples.
- The client supports vector search and ranking helpers in addition to the examples shown here.
