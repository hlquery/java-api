package hlquery.utils;

public class Auth {
    private String token;
    private String method;

    public Auth(String token, String method) {
        this.token = token;
        this.method = method != null ? method : "bearer";
    }

    public String getToken() {
        return token;
    }

    public String getMethod() {
        return method;
    }

    public void setToken(String token, String method) {
        this.token = token;
        this.method = method != null ? method : "bearer";
    }

    public void clear() {
        this.token = null;
        this.method = "bearer";
    }

    public boolean hasAuth() {
        return token != null && !token.isEmpty();
    }
}
