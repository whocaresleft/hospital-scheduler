package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.model.Shift;

public class ShiftNotFoundException extends RuntimeException {
    
    private transient final Shift shift;
    
    public ShiftNotFoundException(Shift shift) {
        super("No shift was found: " + shift);
        this.shift = shift;
    }
    
    private static final long serialVersionUID = 1L;

    public Shift getShift() { return shift; }
}
