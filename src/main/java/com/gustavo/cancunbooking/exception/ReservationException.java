package com.gustavo.cancunbooking.exception;

public class ReservationException extends RuntimeException {

    public ReservationException() {
    }

    public ReservationException(String message) {
        super(message);
    }
}
