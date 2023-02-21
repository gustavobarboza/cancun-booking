package com.gustavo.cancunbooking.controllers.advice;

import com.gustavo.cancunbooking.exceptions.ReservationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorMessage> handleReservationException(ReservationException ex) {
        return ResponseEntity.badRequest().body(new ErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorList> handleInvalidArgumentsException(MethodArgumentNotValidException ex) {

        List<ErrorField> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.add(new ErrorField(field, message));
        });

        return ResponseEntity.badRequest().body(new ErrorList(errors));
    }
}

record ErrorMessage(String errorMessage) { }

record ErrorField(String field, String message) {}

record ErrorList(List<ErrorField> errors) {}
