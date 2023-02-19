package com.gustavo.cancunbooking.controller.advice;

import com.gustavo.cancunbooking.exception.ReservationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorWrapper> handleReservationException(ReservationException ex) {
        return ResponseEntity.badRequest().body(new ErrorWrapper(ex.getMessage()));
    }
}

record ErrorWrapper(String message) { }
