package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.model.Shift;

public class OverlappedShiftException extends RuntimeException {
    
    private Shift conflicting;
    private Shift overlapped;
    
    public OverlappedShiftException(Shift original, Shift overlapped) {
        super("Shift " + overlapped + " overlaps with " + original);
        this.conflicting = original;
        this.overlapped = overlapped;
    }
    
    private static final long serialVersionUID = 1L;
    
    public Shift getConflictingShift() { return conflicting; }
    public Shift getOverlappedShift() { return overlapped; }
}
