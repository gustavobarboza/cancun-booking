package com.gustavo.cancunbooking.controllers;

import com.gustavo.cancunbooking.controllers.request.ReservationRequestDTO;
import com.gustavo.cancunbooking.controllers.request.ReservationUpdateRequestDTO;
import com.gustavo.cancunbooking.controllers.request.RoomAvailabilityRequestDTO;
import com.gustavo.cancunbooking.controllers.response.ReservationSuccessResponseDTO;
import com.gustavo.cancunbooking.controllers.response.RoomAvailabilityResponseDTO;
import com.gustavo.cancunbooking.services.ReservationService;
import com.gustavo.cancunbooking.services.RoomAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/reservation")
public class
ReservationController {
    private final ReservationService reservationService;
    private final RoomAvailabilityService roomAvailabilityService;

    @Autowired
    public ReservationController(ReservationService reservationService, RoomAvailabilityService roomAvailabilityService) {
        this.reservationService = reservationService;
        this.roomAvailabilityService = roomAvailabilityService;
    }

    @PostMapping("new")
    public ResponseEntity<ReservationSuccessResponseDTO> placeNewReservation(@RequestBody @Valid ReservationRequestDTO reservationRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.placeReservation(reservationRequest));
    }

    @PostMapping("update")
    public ResponseEntity<ReservationSuccessResponseDTO> updateReservation(@RequestBody @Valid ReservationUpdateRequestDTO reservationUpdateRequest) {
        return ResponseEntity.ok(reservationService.updateReservation(reservationUpdateRequest));
    }

    @PostMapping("check-availability")
    public ResponseEntity<RoomAvailabilityResponseDTO> checkAvailability(@RequestBody @Valid RoomAvailabilityRequestDTO roomAvailabilityRequest)  {
        return ResponseEntity.ok(roomAvailabilityService
                .getRoomAvailability(roomAvailabilityRequest.getStartDate(), roomAvailabilityRequest.getRoomId())
        );
    }

    @PostMapping("cancel/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
