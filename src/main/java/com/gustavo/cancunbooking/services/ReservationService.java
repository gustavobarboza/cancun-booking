package com.gustavo.cancunbooking.services;

import com.gustavo.cancunbooking.controllers.request.ReservationRequestDTO;
import com.gustavo.cancunbooking.controllers.request.ReservationUpdateRequestDTO;
import com.gustavo.cancunbooking.controllers.response.ReservationSuccessResponseDTO;

public interface ReservationService {
    ReservationSuccessResponseDTO placeReservation(ReservationRequestDTO reservationRequest);
    ReservationSuccessResponseDTO updateReservation(ReservationUpdateRequestDTO reservationUpdateRequest);
    void cancelReservation(Long reservationId);
}
