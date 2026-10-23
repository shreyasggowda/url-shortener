package com.example.urlshortener.api;

import com.example.urlshortener.service.UrlNotFoundException;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(UrlNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(UrlNotFoundException ex) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage())); }
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalid(org.springframework.web.bind.MethodArgumentNotValidException ex) { return ResponseEntity.badRequest().body(Map.of("error", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage())); }
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    ResponseEntity<Map<String, String>> invalidLogin() { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Incorrect email or password")); }
}
