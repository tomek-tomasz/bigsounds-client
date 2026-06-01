package com.example.bigsoundsclient;

public class AuthState {
    private static final AuthState INSTANCE = new AuthState();
    private String token;
    private int userId = -1;

    private AuthState() {}

    public static AuthState getInstance() { return INSTANCE; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void clear() { this.token = null; this.userId = -1; }
    public boolean isLoggedIn() { return token != null; }
}
