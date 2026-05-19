package com.example.gym.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Map<String, Object>> handleAll(Throwable ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        body.put("type", ex.getClass().getName());
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        body.put("stacktrace", sw.toString());
        
        if (ex.getCause() != null) {
            body.put("cause", ex.getCause().getMessage());
            body.put("cause_type", ex.getCause().getClass().getName());
        }
        
        return ResponseEntity.status(500).body(body);
    }
}
