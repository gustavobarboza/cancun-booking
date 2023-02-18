package com.gustavo.cancunbooking.service;

import com.gustavo.cancunbooking.controller.request.ReservationRequestDTO;
import com.gustavo.cancunbooking.controller.response.ReservationSuccessResponseDTO;

import java.time.LocalDate;

// TODO move DTOs to another layer so the service layer doesn't know about the controller layer
public interface ReservationService {
    ReservationSuccessResponseDTO placeReservation(ReservationRequestDTO reservationRequest);
}
