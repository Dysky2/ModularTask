package net.edu.modulartask.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //User
    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<Map<String,String>> handleAccountDisabled(AccountDisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    //Task
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleTaskNotFound(TaskNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyAssignedException.class)
    public ResponseEntity<Map<String,String>> handleUserAlreadyAssigned(UserAlreadyAssignedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedTaskActionException.class)
    public ResponseEntity<Map<String,String>> handleUnauthorizedTaskAction(UnauthorizedTaskActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidDeadlineException.class)
    public ResponseEntity<Map<String,String>> handleInvalidDeadline(InvalidDeadlineException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String,String>> handleDuplicateEmailException(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    //Organization
    @ExceptionHandler(OrganizationUnitNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleOrganizationUnitNotFound(OrganizationUnitNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CyclicHierarchyException.class)
    public ResponseEntity<Map<String,String>> handleCyclicHierarchy(CyclicHierarchyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedOrganizationActionException.class)
    public ResponseEntity<Map<String,String>> handleUnauthorizedOrganizationAction(UnauthorizedOrganizationActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

}
