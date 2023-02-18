package com.gustavo.cancunbooking.service;

import com.gustavo.cancunbooking.controller.response.RoomAvailabilityResponseDTO;
import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {
    private final ReservationRepository reservationRepository;

    @Autowired
    public RoomAvailabilityServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public RoomAvailabilityResponseDTO getRoomAvailability(LocalDate startDate, Long roomId) {
        Optional<Reservation> reservationOptional =
                reservationRepository.findByRoomIdAndStatus(roomId, ReservationStatusEnum.ACTIVE);

        if (reservationOptional.isPresent()) {
            Reservation reservation = reservationOptional.get();
            return getRoomAvailability(startDate, reservation);
        } else {
            return new RoomAvailabilityResponseDTO(true, LocalDate.now().plusDays(1));
        }
    }

    private static RoomAvailabilityResponseDTO getRoomAvailability(LocalDate startDate, Reservation reservation) {
        LocalDate closestAvailableStartDate = reservation.getEndDate().plusDays(1);
        if (roomIsReserved(startDate, reservation)) {
            return new RoomAvailabilityResponseDTO(false, closestAvailableStartDate);
        } else {
            return new RoomAvailabilityResponseDTO(true, closestAvailableStartDate);
        }
    }

    private static boolean roomIsReserved(LocalDate startDate, Reservation reservation) {
        return reservation.getEndDate().isEqual(startDate) || reservation.getEndDate().isAfter(startDate);
    }
}
