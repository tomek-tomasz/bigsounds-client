package com.example.bigsoundsclient;

public class AuthState {
    private static final AuthState INSTANCE = new AuthState();
    private String token;

    private AuthState() {}

    public static AuthState getInstance() { return INSTANCE; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public void clear() { this.token = null; }
    public boolean isLoggedIn() { return token != null; }
}
