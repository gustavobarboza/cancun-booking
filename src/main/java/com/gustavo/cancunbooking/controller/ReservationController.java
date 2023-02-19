package com.gustavo.cancunbooking.controller;

import com.gustavo.cancunbooking.controller.request.ReservationRequestDTO;
import com.gustavo.cancunbooking.controller.request.ReservationUpdateRequestDTO;
import com.gustavo.cancunbooking.controller.response.ReservationSuccessResponseDTO;
import com.gustavo.cancunbooking.controller.response.RoomAvailabilityResponseDTO;
import com.gustavo.cancunbooking.service.ReservationService;
import com.gustavo.cancunbooking.service.RoomAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final RoomAvailabilityService roomAvailabilityService;

    @Autowired
    public ReservationController(ReservationService reservationService, RoomAvailabilityService roomAvailabilityService) {
        this.reservationService = reservationService;
        this.roomAvailabilityService = roomAvailabilityService;
    }

    @PostMapping("new")
    public ResponseEntity<ReservationSuccessResponseDTO> placeNewReservation(@RequestBody ReservationRequestDTO reservationRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.placeReservation(reservationRequest));
    }

    @PostMapping("update")
    public ResponseEntity<ReservationSuccessResponseDTO> updateReservation(@RequestBody ReservationUpdateRequestDTO reservationUpdateRequest) {
        return ResponseEntity.ok(reservationService.updateReservation(reservationUpdateRequest));
    }

    @GetMapping("check-availability")
    public ResponseEntity<RoomAvailabilityResponseDTO> checkAvailability(
            @RequestParam("startDate") LocalDate startDate, @RequestParam("roomId") Long roomId)  {
        if( startDate == null || roomId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(roomAvailabilityService.getRoomAvailability(startDate, roomId));
    }

    @PostMapping("cancel/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
