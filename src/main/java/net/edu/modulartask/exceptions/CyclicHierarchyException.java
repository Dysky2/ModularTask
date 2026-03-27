package net.edu.modulartask.exceptions;

public class CyclicHierarchyException extends RuntimeException {
    public CyclicHierarchyException(String message) {
        super(message);
    }
}
