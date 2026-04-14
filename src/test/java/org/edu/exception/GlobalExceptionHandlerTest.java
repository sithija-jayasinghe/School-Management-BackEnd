package org.edu.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.edu.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnNotFoundForMissingResource() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
            new ResourceNotFoundException("User not found")
        );

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void shouldReturnBadRequestForIllegalArgument() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
            new IllegalArgumentException("User must have STUDENT role")
        );

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("User must have STUDENT role", response.getBody().getMessage());
    }

    @Test
    void shouldReturnConflictForIllegalState() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(
            new IllegalStateException("Student already inactive")
        );

        assertEquals(CONFLICT, response.getStatusCode());
        assertEquals("Student already inactive", response.getBody().getMessage());
    }

    @Test
    void shouldReturnUnauthorizedForUnauthorizedException() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(
            new UnauthorizedException("Unauthorized access")
        );

        assertEquals(UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized access", response.getBody().getMessage());
    }
}
