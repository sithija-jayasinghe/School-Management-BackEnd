package org.edu.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.edu.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

    @Test
    void shouldReturnNotFoundForMissingResource() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
            new ResourceNotFoundException("User not found"),
            request
        );

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getCode());
        assertEquals("User not found", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldReturnBadRequestForIllegalArgument() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
            new IllegalArgumentException("User must have STUDENT role"),
            request
        );

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_ARGUMENT", response.getBody().getCode());
        assertEquals("User must have STUDENT role", response.getBody().getMessage());
    }

    @Test
    void shouldReturnConflictForIllegalState() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(
            new IllegalStateException("Student already inactive"),
            request
        );

        assertEquals(CONFLICT, response.getStatusCode());
        assertEquals("INVALID_STATE", response.getBody().getCode());
        assertEquals("Student already inactive", response.getBody().getMessage());
    }

    @Test
    void shouldReturnUnauthorizedForUnauthorizedException() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(
            new UnauthorizedException("Unauthorized access"),
            request
        );

        assertEquals(UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().getCode());
        assertEquals("Unauthorized access", response.getBody().getMessage());
    }
}
