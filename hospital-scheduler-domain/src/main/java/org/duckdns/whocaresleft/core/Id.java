package org.duckdns.whocaresleft.core;

public class Id {

    private Id() {}
    
    public static Id createId(String value) {
        
        if (value == null) throw new IllegalArgumentException("Id value cannot be null");
        if (value == "") throw new IllegalArgumentException("Id value cannot be empty");
        if (value.trim().isEmpty())  throw new IllegalArgumentException("Id value cannot be empty");
        
        return null;
    }
}
