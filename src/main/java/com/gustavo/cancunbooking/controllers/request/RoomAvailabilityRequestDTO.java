package com.gustavo.cancunbooking.controllers.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomAvailabilityRequestDTO {
    @NotNull(message = "Start date cannot be null")
    @Future(message = "Start date cannot be in the past")
    private LocalDate startDate;
    @NotNull(message = "Room id cannot be null")
    private Long roomId;
}
