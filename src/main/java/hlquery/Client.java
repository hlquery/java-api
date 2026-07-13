package hlquery;

import hlquery.utils.Config;
import hlquery.utils.Validator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private final Request request;
    private final Collections collections;
    private final Documents documents;
    private final Search search;

    public Client(String baseUrl) {
        this(baseUrl, new HashMap<>());
    }

    public Client(String baseUrl, Map<String, Object> options) {
        options = Config.mergeDefaults(options);
        
        String url = (baseUrl != null) ? baseUrl : (String) options.get("base_url");
        url = Config.normalizeUrl(url);
        
        if (!Config.isValidUrl(url)) {
            throw new IllegalArgumentException("Invalid base URL: " + url);
        }
        
        int timeout = (int) options.get("timeout");
        String token = (String) options.get("token");
        String authMethod = (String) options.get("auth_method");
        
        this.request = new Request(url, timeout, token, authMethod);
        this.collections = new Collections(this.request);
        this.documents = new Documents(this.request);
        this.search = new Search(this.request, this.collections);
    }

    public void setAuthToken(String token, String method) {
        this.request.setAuthToken(token, method);
    }

    public void clearAuth() {
        this.request.clearAuth();
    }

    // Cluster & Node APIs
    public Response health() {
        return request.execute("GET", "/health");
    }

    public Response ready() {
        return request.execute("GET", "/ready");
    }

    public Response stats() {
        return request.execute("GET", "/stats");
    }

    public Response status() {
        return request.execute("GET", "/status");
    }

    public Response query() {
        return request.execute("GET", "/query");
    }

    public Response startup() {
        return request.execute("GET", "/startup");
    }

    public Response bootStatus() {
        return request.execute("GET", "/boot-status");
    }

    public Response metrics() {
        return request.execute("GET", "/metrics");
    }

    public Response metricsJson() {
        return request.execute("GET", "/metrics.json");
    }

    public Response metricsHistory() {
        return request.execute("GET", "/metrics/history");
    }

    public Response metricsHistoryAlias() {
        return request.execute("GET", "/metrics-history");
    }

    public Response cache() {
        return request.execute("GET", "/cache");
    }

    public Response connections() {
        return request.execute("GET", "/connections");
    }

    public Response rocksdb() {
        return request.execute("GET", "/rocksdb");
    }

    public Response rocksdbInternal() {
        return request.execute("GET", "/_rocksdb");
    }

    public Response docTotal() {
        return request.execute("GET", "/doctotal");
    }

    public Response ping() {
        return request.execute("GET", "/ping");
    }

    public Response integrity() {
        return request.execute("GET", "/integrity");
    }

    public Response consistency() {
        return request.execute("GET", "/consistency");
    }

    public Response selfCheck() {
        return request.execute("GET", "/self-check");
    }

    public Response storageStatus() {
        return request.execute("GET", "/admin/storage_status");
    }

    public Response searchConfig() {
        return request.execute("GET", "/search-config");
    }

    public Response configFiles() {
        return request.execute("GET", "/config-files");
    }

    public Response updateCounters() {
        return request.execute("GET", "/update-counters");
    }

    public Response updateCounters(JSONObject body) {
        return request.execute("POST", "/update-counters", body != null ? body : new JSONObject());
    }

    public Response debugCounters() {
        return request.execute("GET", "/debug/counters");
    }

    public Response repair() {
        return request.execute("GET", "/repair");
    }

    public Response repair(JSONObject body) {
        return request.execute("POST", "/repair", body != null ? body : new JSONObject());
    }

    public Response etc() {
        return request.execute("GET", "/etc");
    }

    public Response sql(String sql) {
        return sql(sql, new HashMap<>());
    }

    public Response sql(String sql, Map<String, String> queryParams) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query must be a non-empty string");
        }

        Map<String, String> params = new HashMap<>();
        if (queryParams != null) {
            params.putAll(queryParams);
        }
        params.put("sql", sql);
        return request.execute("GET", "/sql", null, params);
    }

    public Response execSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query must be a non-empty string");
        }

        JSONObject body = new JSONObject();
        body.put("exec", sql);
        return request.execute("POST", "/sql", body);
    }

    public Response sqlPost(String sql) {
        return sqlPost(sql, new HashMap<>());
    }

    public Response sqlPost(String sql, Map<String, Object> params) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query must be a non-empty string");
        }

        JSONObject body = new JSONObject();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    body.put(entry.getKey(), entry.getValue());
                }
            }
        }
        body.put("sql", sql);
        return request.execute("POST", "/sql", body);
    }

    public Response sqlExec(String sql) {
        return execSql(sql);
    }

    public Response sqlSelect(String sql) {
        return sql(sql);
    }

    public Response sqlWrite(String sql) {
        return sqlPost(sql);
    }

    public Response info() {
        return request.execute("GET", "/");
    }

    public Response clusterHealth() {
        return health();
    }

    public Response clusterStats() {
        return stats();
    }

    public Response clusterNodes() {
        return links();
    }

    public Response links() {
        return request.execute("GET", "/links");
    }

    public Response linksPing() {
        return request.execute("GET", "/links/ping");
    }

    public Response linksConnect(String endpointOrHost, Integer port) {
        JSONObject body = new JSONObject();
        if (port != null) {
            body.put("host", endpointOrHost);
            body.put("port", port);
        } else {
            body.put("endpoint", endpointOrHost);
        }
        return request.execute("POST", "/links/connect", body);
    }

    public Response linksDisconnect(String endpointOrHost, Integer port) {
        JSONObject body = new JSONObject();
        if (port != null) {
            body.put("host", endpointOrHost);
            body.put("port", port);
        } else {
            body.put("endpoint", endpointOrHost);
        }
        return request.execute("POST", "/links/disconnect", body);
    }

    public Response flush() {
        return request.execute("POST", "/flush");
    }

    public Response listUsers() {
        return request.execute("GET", "/users");
    }

    public Response getUser(String id) {
        validateId(id, "User ID");
        return request.execute("GET", "/users/" + encode(id));
    }

    public Response createUser(JSONObject body) {
        return request.execute("POST", "/users", body);
    }

    public Response updateUser(String id, JSONObject body) {
        validateId(id, "User ID");
        return request.execute("PUT", "/users/" + encode(id), body);
    }

    public Response deleteUser(String id) {
        validateId(id, "User ID");
        return request.execute("DELETE", "/users/" + encode(id));
    }

    public Response listKeys() {
        return request.execute("GET", "/keys");
    }

    public Response getKey(String id) {
        validateId(id, "Key ID");
        return request.execute("GET", "/keys/" + encode(id));
    }

    public Response createKey(JSONObject body) {
        return request.execute("POST", "/keys", body);
    }

    public Response updateKey(String id, JSONObject body) {
        validateId(id, "Key ID");
        return request.execute("PUT", "/keys/" + encode(id), body);
    }

    public Response deleteKey(String id) {
        validateId(id, "Key ID");
        return request.execute("DELETE", "/keys/" + encode(id));
    }

    public Response listAliases() {
        return request.execute("GET", "/aliases");
    }

    public Response listCollectionAliases(String collectionName) {
        Validator.validateCollectionName(collectionName);
        return request.execute("GET", "/collections/" + encode(collectionName) + "/aliases");
    }

    public Response getAlias(String name) {
        validateId(name, "Alias name");
        return request.execute("GET", "/aliases/" + encode(name));
    }

    public Response createAlias(String name, JSONObject body) {
        validateId(name, "Alias name");
        return request.execute("POST", "/aliases/" + encode(name), body);
    }

    public Response updateAlias(String name, JSONObject body) {
        validateId(name, "Alias name");
        return request.execute("PUT", "/aliases/" + encode(name), body);
    }

    public Response deleteAlias(String name) {
        validateId(name, "Alias name");
        return request.execute("DELETE", "/aliases/" + encode(name));
    }

    public Response listOverrides(String collectionName) {
        Validator.validateCollectionName(collectionName);
        return request.execute("GET", "/collections/" + encode(collectionName) + "/overrides");
    }

    public Response getOverride(String collectionName, String id) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(id);
        return request.execute("GET", "/collections/" + encode(collectionName) + "/overrides/" + encode(id));
    }

    public Response createOverride(String collectionName, String id, JSONObject body) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(id);
        return request.execute("POST", "/collections/" + encode(collectionName) + "/overrides/" + encode(id), body);
    }

    public Response updateOverride(String collectionName, String id, JSONObject body) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(id);
        return request.execute("PUT", "/collections/" + encode(collectionName) + "/overrides/" + encode(id), body);
    }

    public Response deleteOverride(String collectionName, String id) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(id);
        return request.execute("DELETE", "/collections/" + encode(collectionName) + "/overrides/" + encode(id));
    }

    public Response listSynonyms(String collectionName) {
        Validator.validateCollectionName(collectionName);
        return request.execute("GET", "/collections/" + encode(collectionName) + "/synonyms");
    }

    public Response getSynonym(String collectionName, String id) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(id);
        return request.execute("GET", "/collections/" + encode(collectionName) + "/synonyms/" + encode(id));
    }

    public Response createSynonym(String collectionName, String id, JSONObject body) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(id);
        return request.execute("POST", "/collections/" + encode(collectionName) + "/synonyms/" + encode(id), body);
    }

    public Response updateSynonym(String collectionName, String id, JSONObject body) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(id);
        return request.execute("PUT", "/collections/" + encode(collectionName) + "/synonyms/" + encode(id), body);
    }

    public Response deleteSynonym(String collectionName, String id) {
        Validator.validateCollectionName(collectionName);
        Validator.validateDocumentId(id);
        return request.execute("DELETE", "/collections/" + encode(collectionName) + "/synonyms/" + encode(id));
    }

    public Response listAllSynonyms() {
        return request.execute("GET", "/synonyms");
    }

    public Response listGlobalSynonyms() {
        return request.execute("GET", "/synonyms/global");
    }

    public Response getGlobalSynonym(String id) {
        Validator.validateDocumentId(id);
        return request.execute("GET", "/synonyms/global/" + encode(id));
    }

    public Response createGlobalSynonym(String id, JSONObject body) {
        Validator.validateDocumentId(id);
        return request.execute("POST", "/synonyms/global/" + encode(id), body);
    }

    public Response updateGlobalSynonym(String id, JSONObject body) {
        Validator.validateDocumentId(id);
        return request.execute("PUT", "/synonyms/global/" + encode(id), body);
    }

    public Response deleteGlobalSynonym(String id) {
        Validator.validateDocumentId(id);
        return request.execute("DELETE", "/synonyms/global/" + encode(id));
    }

    public Response listStopwords(String collectionName) {
        Validator.validateCollectionName(collectionName);
        return request.execute("GET", "/collections/" + encode(collectionName) + "/stopwords");
    }

    public Response createStopword(String collectionName, JSONObject body) {
        Validator.validateCollectionName(collectionName);
        return request.execute("POST", "/collections/" + encode(collectionName) + "/stopwords", body);
    }

    public Response deleteStopword(String collectionName, String word) {
        Validator.validateCollectionName(collectionName);
        validateId(word, "Stopword");
        return request.execute("DELETE", "/collections/" + encode(collectionName) + "/stopwords/" + encode(word));
    }

    public Response listAllStopwords() {
        return request.execute("GET", "/stopwords");
    }

    public Response listGlobalStopwords() {
        return request.execute("GET", "/stopwords/global");
    }

    public Response createGlobalStopword(JSONObject body) {
        return request.execute("POST", "/stopwords/global", body);
    }

    public Response deleteGlobalStopword(String word) {
        validateId(word, "Stopword");
        return request.execute("DELETE", "/stopwords/global/" + encode(word));
    }

    public Response listModules() {
        return request.execute("GET", "/modules");
    }

    public Response loadModule(String name) {
        validateId(name, "Module name");
        return request.execute("POST", "/loadmodule/" + encode(name), new JSONObject());
    }

    public Response unloadModule(String name) {
        validateId(name, "Module name");
        return request.execute("POST", "/unloadmodule/" + encode(name), new JSONObject());
    }

    public Response loadModuleAlias(String name) {
        validateId(name, "Module name");
        return request.execute("POST", "/modules/load/" + encode(name), new JSONObject());
    }

    public Response unloadModuleAlias(String name) {
        validateId(name, "Module name");
        return request.execute("POST", "/modules/unload/" + encode(name), new JSONObject());
    }

    public Response loadModuleLegacy(String name) {
        return loadModule(name);
    }

    public Response unloadModuleLegacy(String name) {
        return unloadModule(name);
    }

    public Response moduleSyntax(String name) {
        validateId(name, "Module name");
        return request.execute("GET", "/modules/" + encode(name) + "/syntax");
    }

    public Response moduleCall(String name, String route, String method, Object body, Map<String, String> queryParams) {
        validateId(name, "Module name");
        String suffix = (route == null || route.isEmpty()) ? "" : "/" + route.replaceFirst("^/+", "");
        return request.execute(method != null ? method : "GET", "/modules/" + encode(name) + suffix, body, queryParams);
    }

    // API Instances
    public Collections collections() {
        return collections;
    }

    public Documents documents() {
        return documents;
    }

    public Search searchApi() {
        return search;
    }

    // Convenience Methods
    public Response listCollections(int offset, int limit) {
        return collections.list(offset, limit);
    }

    public Response listCollectionsDistributed() {
        return request.execute("GET", "/collections/distributed");
    }

    public Response getCollection(String name) {
        return collections.get(name);
    }

    public Response getCollectionFields(String name) {
        return collections.getFields(name);
    }

    public Response getCollectionLanguage(String name) {
        return collections.getLanguage(name);
    }

    public Response listDocuments(String collectionName, Map<String, Object> params) {
        return documents.list(collectionName, params);
    }

    public Response getDocument(String collectionName, String documentId) {
        return documents.get(collectionName, documentId);
    }

    public Response exportDocuments(String collectionName, Map<String, Object> params) {
        return documents.export(collectionName, params);
    }

    public Response deleteAllDocuments(String collectionName) {
        return documents.deleteAll(collectionName);
    }

    public Response updateByQuery(String collectionName, JSONObject body) {
        return documents.updateByQuery(collectionName, body);
    }

    public Response deleteByQuery(String collectionName, JSONObject body) {
        return documents.deleteByQuery(collectionName, body);
    }

    public Response facetCounts(String collectionName, Map<String, Object> params) {
        return documents.facetCounts(collectionName, params);
    }

    public Response maybe(String collectionName, Map<String, Object> params) {
        return documents.maybe(collectionName, params);
    }

    public Response documentContext(String collectionName, String documentId, Map<String, Object> params) {
        return documents.context(collectionName, documentId, params);
    }

    public Response search(String collectionName, Map<String, Object> params) {
        return search.search(collectionName, params);
    }

    public Response sqlSearch(String collectionName, String sql) {
        return sqlSearch(collectionName, sql, new HashMap<>());
    }

    public Response sqlSearch(String collectionName, String sql, Map<String, Object> params) {
        return search.sql(collectionName, sql, params);
    }

    public Response sqlSearchPost(String collectionName, String sql) {
        return sqlSearchPost(collectionName, sql, new HashMap<>());
    }

    public Response sqlSearchPost(String collectionName, String sql, Map<String, Object> params) {
        return search.sqlPost(collectionName, sql, params);
    }

    public Response vectorSearch(String collectionName, Map<String, Object> params) {
        return search.vectorSearch(collectionName, params);
    }

    public Response globalSearch(Map<String, Object> params) {
        return search.globalSearch(params);
    }

    public Response multiSearch(JSONArray searches) {
        return search.multiSearch(searches);
    }

    public Response multiSearch(JSONObject body) {
        return search.multiSearch(body);
    }

    public Response analyticsClick(JSONObject body) {
        return request.execute("POST", "/analytics/click", body != null ? body : new JSONObject());
    }

    public Response executeRequest(String method, String path, Object body, Map<String, String> queryParams) {
        return request.execute(method, path, body, queryParams);
    }

    public Response executeRequestSafe(String method, String path, Object body, Map<String, String> queryParams) {
        return request.executeSafe(method, path, body, queryParams);
    }

    public Response executeRequestWithRetry(
            String method,
            String path,
            Object body,
            Map<String, String> queryParams,
            int maxRetries,
            long initialBackoffMillis
    ) {
        return request.executeWithRetry(method, path, body, queryParams, maxRetries, initialBackoffMillis);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void validateId(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " must be a non-empty string");
        }
    }

    private Map<String, String> toQueryParams(Map<String, Object> params) {
        Map<String, String> queryParams = new HashMap<>();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    queryParams.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        return queryParams;
    }
}
