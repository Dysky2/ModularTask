package net.edu.modulartask.exceptions;

public class UnauthorizedTaskActionException extends RuntimeException {
    public UnauthorizedTaskActionException(String message) {
        super(message);
    }
}
