package com.gustavo.cancunbooking.controllers.response;

import com.gustavo.cancunbooking.model.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSuccessResponseDTO {

    public ReservationSuccessResponseDTO(Reservation reservation) {
        this.reservationId = reservation.getId();
        this.userId = reservation.getUser().getId();
        this.roomId = reservation.getRoom().getId();
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
    }

    private Long reservationId;
    private Long userId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
}
