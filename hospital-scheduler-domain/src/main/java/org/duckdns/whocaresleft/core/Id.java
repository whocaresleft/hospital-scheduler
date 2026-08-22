package org.duckdns.whocaresleft.core;

public class Id {

    private Id() {}
    
    public static Id createId(String value) {
        
        if (value == null) throw new IllegalArgumentException("Id value cannot be null");
        
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Id value cannot be empty");
        
        if (!trimmed.matches("[a-zA-Z0-9_]+")) throw new IllegalArgumentException("Id value contains invalid characters");
        
        return null;
    }
}
