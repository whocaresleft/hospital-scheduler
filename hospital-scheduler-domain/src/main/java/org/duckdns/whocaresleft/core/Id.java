package org.duckdns.whocaresleft.core;

public class Id {

    private String value;
    
    private Id(String value) { setValue(value); }
    
    public static Id createId(String value) {
        
        if (value == null) throw new IllegalArgumentException("Id value cannot be null");
        
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Id value cannot be empty");
        
        if (!trimmed.matches("\\w+")) throw new IllegalArgumentException("Id value contains invalid characters");
        
        return new Id(trimmed);
    }

    public String getValue() { return value; }
    
    private void setValue(String value) { this.value = value; }
    
    @Override
    public boolean equals(Object other) {
        
        if (this == other)
            return true;
        
        if (other == null)
            return false;
        
        if (getClass() != other.getClass())
            return false;
        
        Id otherId = (Id)other;
        
        return value
            .equals(otherId.value);
    }
    
    @Override
    public int hashCode() { return value.hashCode(); }
    
    @Override
    public String toString() { return value; }
}
