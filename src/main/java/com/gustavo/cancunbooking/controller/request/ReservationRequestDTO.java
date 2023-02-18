package com.gustavo.cancunbooking.controller.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// TODO change to records
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {
    @NotNull(message = "User id cannot be null")
    private Long userId;
    @NotNull(message = "Room id cannot be null")
    private Long roomId;

    @NotNull(message = "Start date cannot be null")
    @Future( message = "Start date has to be in the future")
    private LocalDate startDate;
    @NotNull(message = "End date cannot be null")
    @Future(message = "End date has to be in the future")
    private LocalDate endDate;
}
