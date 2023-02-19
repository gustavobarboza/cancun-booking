package com.gustavo.cancunbooking.controllers.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateRequestDTO {
    @NotNull(message = "Reservation id cannot be null")
    private Long reservationId;

    @NotNull(message = "Start date cannot be null")
    @Future( message = "Start date has to be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @Future(message = "End date has to be in the future")
    private LocalDate endDate;
}
