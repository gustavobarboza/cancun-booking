package com.gustavo.cancunbooking.services;

import com.gustavo.cancunbooking.controllers.response.RoomAvailabilityResponseDTO;
import com.gustavo.cancunbooking.exceptions.ReservationException;
import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.repositories.ReservationRepository;
import com.gustavo.cancunbooking.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public RoomAvailabilityServiceImpl(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomAvailabilityResponseDTO getRoomAvailability(LocalDate startDate, Long roomId) {
        Optional<Reservation> reservationOptional =
                reservationRepository.findByRoomIdAndStatus(roomId, ReservationStatusEnum.ACTIVE);

        if (reservationOptional.isPresent()) {
            Reservation reservation = reservationOptional.get();
            return getRoomAvailability(startDate, reservation);
        } else {
            if (!roomRepository.existsById(roomId)){
                throw new ReservationException("No room found with the provided id");
            }

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
