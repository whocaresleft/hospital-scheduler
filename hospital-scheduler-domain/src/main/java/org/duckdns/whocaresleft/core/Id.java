package org.duckdns.whocaresleft.core;

import java.util.Objects;

public final class Id {

    private final String value;
    
    private Id(String value) { this.value = value; }
    
    public static Id createId(String value) {
        
        if (value == null) throw new IllegalArgumentException("Id value cannot be null");
        
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Id value cannot be empty");
        
        if (!trimmed.matches("\\w+")) throw new IllegalArgumentException("Id value contains invalid characters");
        
        return new Id(trimmed);
    }

    public String getValue() { return value; }
    
    @Override
    public boolean equals(Object other) {
        
        if (this == other)
            return true;
        
        if (other == null)
            return false;
        
        if (getClass() != other.getClass())
            return false;
        
        Id otherId = (Id)other;
        
        return Objects.equals(value, otherId.value);
    }
    
    @Override
    public int hashCode() { return Objects.hashCode(value); }
    
    @Override
    public String toString() { return value; }
}
