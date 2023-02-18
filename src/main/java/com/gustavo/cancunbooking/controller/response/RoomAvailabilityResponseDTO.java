package com.gustavo.cancunbooking.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityResponseDTO {
    private boolean isAvailable;
    private LocalDate closestAvailableStartDate;
}
