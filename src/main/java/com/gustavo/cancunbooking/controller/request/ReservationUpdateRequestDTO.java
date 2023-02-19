package com.gustavo.cancunbooking.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateRequestDTO {
    private Long reservationId;
    private LocalDate startDate;
    private LocalDate endDate;
}
