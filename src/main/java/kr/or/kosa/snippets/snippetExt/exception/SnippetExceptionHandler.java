package kr.or.kosa.snippets.snippetExt.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice(basePackages = "kr.or.kosa.snippets.snippetExt")
public class SnippetExceptionHandler {

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<?> handleUnauthenticated(AuthenticationCredentialsNotFoundException ex) {
        return ResponseEntity.status(401).body(Map.of(
            "error", "Unauthorized",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of(
            "error", "Forbidden",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of(
            "error", "Not Found",
            "message", ex.getMessage()
        ));
    }
}

