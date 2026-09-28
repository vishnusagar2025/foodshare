package com.example.foodshare.exception;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Turns every failure into a clear JSON message instead of a stack trace. */
@RestControllerAdvice
public class ApiExceptionHandler {
    private ResponseEntity<Map<String, Object>> body(HttpStatus s, String msg, Object details) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", LocalDateTime.now().toString());
        m.put("status", s.value());
        m.put("error", msg);
        if (details != null) m.put("details", details);
        return ResponseEntity.status(s).body(m);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(NotFoundException e) { return body(HttpStatus.NOT_FOUND, e.getMessage(), null); }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> rule(BusinessRuleException e) { return body(HttpStatus.CONFLICT, e.getMessage(), null); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST, "Validation failed", fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> malformed(Exception e) {
        return body(HttpStatus.BAD_REQUEST, "Malformed request or invalid value (check dates like 2026-10-01T18:00 and enum values)", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integrity(DataIntegrityViolationException e) {
        return body(HttpStatus.CONFLICT, "Operation not allowed: other records depend on this one", null);
    }
}
