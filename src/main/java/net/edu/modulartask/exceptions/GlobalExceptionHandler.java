package net.edu.modulartask.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //User
    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<Map<String,String>> handleAccountDisabled(AccountDisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
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

    @ExceptionHandler(OrganizationUnitHasChildrenException.class)
    public ResponseEntity<Map<String,String>> handleOrganizationUnitHasChildren(OrganizationUnitHasChildrenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedAdminActionException.class)
    public ResponseEntity<Map<String,String>> handleUnauthorizedAdminAction(UnauthorizedAdminActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String,String>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String,String>> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter: " + ex.getName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }

}
