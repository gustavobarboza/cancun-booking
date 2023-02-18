package com.gustavo.cancunbooking.service;

import com.gustavo.cancunbooking.controller.response.RoomAvailabilityResponseDTO;

import java.time.LocalDate;

public interface RoomAvailabilityService {
    RoomAvailabilityResponseDTO getRoomAvailability(LocalDate startDate, Long roomId);
}
