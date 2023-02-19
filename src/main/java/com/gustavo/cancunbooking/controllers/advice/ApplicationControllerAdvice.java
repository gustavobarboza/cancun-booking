package com.gustavo.cancunbooking.controllers.advice;

import com.gustavo.cancunbooking.exceptions.ReservationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorWrapper> handleReservationException(ReservationException ex) {
        return ResponseEntity.badRequest().body(new ErrorWrapper(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidArgumentsException(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            errors.put(((FieldError)error).getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }
}

record ErrorWrapper(String errorMessage) { }
