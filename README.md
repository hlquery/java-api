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

The hlquery Java API is the official Java client for [hlquery](https://github.com/hlquery/hlquery). It wraps hlquery's HTTP/JSON endpoints in a single client object with helpers for collections, documents, search, vector search, SQL, and SAM.

It is intended for JVM services, tools, and applications that want a straightforward hlquery integration layer instead of manual request construction.

### Why use it?

- Familiar client layout for Java applications.
- Response objects for status checks and raw body access.
- Coverage for the main hlquery endpoint families.
- Simple path for both high-level helpers and raw requests.

### Why choose it over raw HTTP?

Choose the Java client over raw HTTP when you want less repetitive URL, auth, and request-body code, one client entry point for day-to-day hlquery tasks, and a simpler way to keep request formatting and error handling consistent.

### Install

Build with the included `Makefile`:

```bash
$ make
```

This downloads the required `org.json` dependency and compiles the client sources.

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

### Contributing

We welcome contributions from the community! All contributions must be released under the BSD 3-Clause license.

### How to Contribute

- Check existing [issues](https://github.com/hlquery/hlquery/issues) or create new ones
- Contribute to client libraries (Node.js, Go, Java, Python, PHP, Ruby, Rust, Perl, C++)
- Test and report bugs
- Improve documentation

### Community

- 📖 [Documentation](https://docs.hlquery.com)
- 🐦 [X (Twitter)](https://x.com/hlquery)
- 📦 [GitHub](https://github.com/hlquery/hlquery)
