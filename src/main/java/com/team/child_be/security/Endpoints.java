package com.team.child_be.security;

public class Endpoints {
    public static final String API_PREFIX = "";
    
    public static class Public {
        public static final String[] GET = {
            API_PREFIX + "/auth/verify",
            API_PREFIX + "/auth/validate"
        };

        public static final String[] POST = {
            API_PREFIX + "/auth/login",
            API_PREFIX + "/auth/signup",
            API_PREFIX + "/auth/forgot-password",
            API_PREFIX + "/auth/refresh-token",
            API_PREFIX + "/auth/oauth2/**"
        };

        public static final String[] PUT = {
            API_PREFIX + "/auth/reset-password"
        };
    }
    
    public static class Admin {
        public static final String[] GET = {
            API_PREFIX + "/admin/users/**",
            API_PREFIX + "/admin/stats/**"
        };

        public static final String[] POST = {
            API_PREFIX + "/admin/users"
        };

        public static final String[] PUT = {
            API_PREFIX + "/admin/users/**"
        };

        public static final String[] DELETE = {
            API_PREFIX + "/admin/users/**"
        };
    }
    
    public static class User {
        public static final String[] GET = {
            API_PREFIX + "/users/profile"
        };
        
        public static final String[] PUT = {
            API_PREFIX + "/users/profile"
        };
    }
}
