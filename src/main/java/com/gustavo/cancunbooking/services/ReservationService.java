package com.gustavo.cancunbooking.services;

import com.gustavo.cancunbooking.controllers.request.ReservationRequestDTO;
import com.gustavo.cancunbooking.controllers.request.ReservationUpdateRequestDTO;
import com.gustavo.cancunbooking.controllers.response.ReservationSuccessResponseDTO;

// TODO move DTOs to another layer so the service layer doesn't know about the controller layer
public interface ReservationService {
    ReservationSuccessResponseDTO placeReservation(ReservationRequestDTO reservationRequest);
    ReservationSuccessResponseDTO updateReservation(ReservationUpdateRequestDTO reservationUpdateRequest);
    void cancelReservation(Long reservationId);
}
