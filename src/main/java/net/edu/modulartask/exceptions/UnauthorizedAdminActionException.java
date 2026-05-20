package net.edu.modulartask.exceptions;

public class UnauthorizedAdminActionException extends RuntimeException {
    public UnauthorizedAdminActionException(String message) {
        super(message);
    }
}

