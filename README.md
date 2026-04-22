<div align="center">
  <img src="https://docs.hlquery.com/img/hlquery/2.png" alt="hlquery logo" width="200">
</div>

<div align="center">

**A clean, idiomatic java client library for hlquery.**

[![Follow hlquery](https://img.shields.io/badge/Follow-%40hlquery-blue?logo=x&logoColor=white)](https://x.com/hlquery)
[![Commit Activity](https://img.shields.io/github/commit-activity/m/hlquery/java-api)](https://github.com/hlquery/java-api/pulse)
[![java-api](https://img.shields.io/badge/GitHub-java--api-181717?logo=github&logoColor=white)](https://github.com/hlquery/java-api/stargazers)
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

</div>


# hlquery Java API Client

A Java client library for the hlquery search engine.

Supports collections, documents, search, vector search, and SQL helpers.

## Requirements

- Java 11 or higher
- `curl` (to download dependencies)

## Installation

You can build the project using the provided Makefile:

```bash
$ make
```

This will download the necessary `org.json` dependency and compile the source files.

## Usage

```java
import hlquery.Client;
import hlquery.utils.Config;
import hlquery.Response;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Client client = new Client(Config.getDefaultBaseUrl());
        
        // Check health
        Response health = client.health();
        System.out.println(health.getRawBody());
        
        // Search
        Map<String, Object> params = new HashMap<>();
        params.put("q", "search term");
Response results = client.search("my_collection", params);
System.out.println(results.getRawBody());

        // Vector search (POST body)
        Map<String, Object> vectorBody = new HashMap<>();
        vectorBody.put("vector", new double[]{0.1, 0.2, 0.3});
        vectorBody.put("field_name", "embedding");
        vectorBody.put("topk", 5);
        vectorBody.put("include_distance", true);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("ef", 128);
        queryParams.put("nprobe", 8);
        vectorBody.put("query_params", queryParams);

        Map<String, Object> vectorRequest = new HashMap<>();
        vectorRequest.put("body", vectorBody);
        Response vectorResults = client.vectorSearch("my_collection", vectorRequest);
        System.out.println(vectorResults.getRawBody());

        // SQL search
        Response sqlResults = client.sqlSearch(
            "my_collection",
            "SELECT id, title FROM my_collection ORDER BY title ASC LIMIT 3;"
        );
        System.out.println(sqlResults.getRawBody());
}
}
```

## SQL

Basic SQL example:

```java
Client client = new Client(Config.getDefaultBaseUrl());

Response response = client.sqlSearch(
    "products",
    "SELECT id, title, price FROM products ORDER BY price DESC LIMIT 5;"
);

if (response.isSuccess()) {
    System.out.println(response.getRawBody());
}
```

Top-level SQL execution:

```java
Response rows = client.sql("SHOW COLLECTIONS;");

Response insert = client.execSql(
    "INSERT INTO products (id, title, price) VALUES ('sku-9', 'Camp Stove', 89);"
);
```

## Reduce Text Example

You can use the raw request helper to call custom module routes directly:

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

## Ranking Helpers

`hlquery.Ranker` exposes a shared `computeRankSignal` helper and `attachRankSort` shortcut so you can reuse the popularity/hit ranking formula and sort by `rank_signal`.

```java
Map<String, Object> params = new HashMap<>();
params.put("q", "guide");
double signal = Ranker.computeRankSignal(popularity, hitLog, null);
params.put("rank_signal", signal);
Ranker.attachRankSort(params, "rank_signal", "desc");
Response results = client.search("collection", params);
```

## Running Examples

You can run the provided examples using either `make` or direct Java commands.

### Using Make (Recommended)

```bash
# Run the main quickstart example
$ make example

# Run collection management examples
$ make examples-cols

# Run document operation examples
$ make examples-docs

# Run search examples
$ make examples-search

# Run SQL examples
$ make examples-sql
```

### Manual Execution (Without Make)

1. **Compile all source files**:
```bash
mkdir -p bin
javac -cp lib/json.jar -d bin $(find src/main/java -name "*.java")
```

2. **Run a specific example**:
```bash
# Main Example
$ java -cp lib/json.jar:bin hlquery.Example

# Collections Examples
$ java -cp lib/json.jar:bin hlquery.examples.Collections

# Documents Examples
$ java -cp lib/json.jar:bin hlquery.examples.Documents

# Search Examples
$ java -cp lib/json.jar:bin hlquery.examples.Search

# SQL Examples
$ java -cp lib/json.jar:bin hlquery.examples.SQL
```

## Running Tests

```bash
# Using Make
$ make test

# Manually
$ javac -cp lib/json.jar:bin -d bin ../tests/ApiTest.java
$ java -cp lib/json.jar:bin ApiTest
```
