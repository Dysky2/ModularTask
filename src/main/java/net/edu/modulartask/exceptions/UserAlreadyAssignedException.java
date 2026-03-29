package net.edu.modulartask.exceptions;

public class UserAlreadyAssignedException extends RuntimeException{
    public UserAlreadyAssignedException(String message) {
        super(message);
    }
}
