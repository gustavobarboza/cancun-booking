package com.gustavo.cancunbooking.services;

import com.gustavo.cancunbooking.controllers.response.RoomAvailabilityResponseDTO;

import java.time.LocalDate;

public interface RoomAvailabilityService {
    RoomAvailabilityResponseDTO getRoomAvailability(LocalDate startDate, Long roomId);
}
