package net.edu.modulartask.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDeadlineException extends RuntimeException {
    public InvalidDeadlineException(String message) {
        super(message);
    }
}
