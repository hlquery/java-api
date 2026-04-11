<div align="center">
  <img src="https://docs.hlquery.com/img/hlquery/2.png" alt="hlquery logo" width="200">
</div>

<div align="center">

**A clean, idiomatic java client library for hlquery.**

[![Twitter Follow](https://img.shields.io/twitter/url/https/x.com/hlquery.svg?style=social&label=Follow%20%40hlquery)](https://x.com/hlquery)
[![Commit Activity](https://img.shields.io/github/commit-activity/m/hlquery/java-api)](https://github.com/hlquery/java-api/pulse)
[![GitHub stars](https://img.shields.io/github/stars/hlquery/java-api?style=social)](https://github.com/hlquery/java-api/stargazers)
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

[Documentation](https://docs.hlquery.com) • [hlquery](https://github.com/hlquery/hlquery) • [Discord](https://discord.hlquery.com)

</div>


# hlquery Java API Client

A Java client library for the hlquery search engine.

## Requirements

- Java 11 or higher
- `curl` (to download dependencies)

## Installation

You can build the project using the provided Makefile:

```bash
make
```

This will download the necessary `org.json` dependency and compile the source files.

## Usage

```java
import hlquery.Client;
import hlquery.Response;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Client client = new Client("http://localhost:9200");
        
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
}
}
```

## Reduce Text Example

If the `ai_search` module is enabled, you can use the raw request helper to summarize a stored document:

```java
Map<String, String> query = new HashMap<>();
query.put("q", "summarize onboarding guide in docs");
query.put("run", "true");

Response summary = client.executeRequest(
    "GET",
    "/modules/ai_search/talk",
    null,
    query
);

System.out.println(summary.getRawBody());
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
make example

# Run collection management examples
make examples-cols

# Run document operation examples
make examples-docs

# Run search examples
make examples-search
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
java -cp lib/json.jar:bin hlquery.Example

# Collections Examples
java -cp lib/json.jar:bin hlquery.examples.Collections

# Documents Examples
java -cp lib/json.jar:bin hlquery.examples.Documents

# Search Examples
java -cp lib/json.jar:bin hlquery.examples.Search
```

## Running Tests

```bash
# Using Make
make test

# Manually
javac -cp lib/json.jar:bin -d bin ../tests/ApiTest.java
java -cp lib/json.jar:bin ApiTest
```
