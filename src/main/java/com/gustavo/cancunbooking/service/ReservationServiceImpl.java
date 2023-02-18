package com.gustavo.cancunbooking.service;

import com.gustavo.cancunbooking.controller.request.ReservationRequestDTO;
import com.gustavo.cancunbooking.controller.response.ReservationSuccessResponseDTO;
import com.gustavo.cancunbooking.exception.ReservationAfterAllowedMaximumException;
import com.gustavo.cancunbooking.exception.ReservationTooLongException;
import com.gustavo.cancunbooking.exception.RoomAlreadyReservedException;
import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.repository.ReservationRepository;
import com.gustavo.cancunbooking.repository.RoomRepository;
import com.gustavo.cancunbooking.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ReservationServiceImpl implements ReservationService {

    public static final int MAXIMUM_RESERVATION_DAS_ALLOWED = 3;

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final RoomAvailabilityService roomAvailabilityService;
    private final Clock clock;
    @Autowired
    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            UserRepository userRepository,
            RoomAvailabilityService roomAvailabilityService,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomAvailabilityService = roomAvailabilityService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ReservationSuccessResponseDTO placeReservation(ReservationRequestDTO reservationRequest) {
        validateReservationRequest(reservationRequest);

        Reservation reservation = new Reservation();
        reservation.setStartDate(reservationRequest.getStartDate());
        reservation.setEndDate(reservationRequest.getEndDate());
        reservation.setStatus(ReservationStatusEnum.ACTIVE);

        // TODO check for the existence of these before trying to set them
        reservation.setRoom(roomRepository.getReferenceById(reservationRequest.getRoomId()));
        reservation.setUser(userRepository.getReferenceById(reservationRequest.getUserId()));

        reservation = reservationRepository.save(reservation);
        return new ReservationSuccessResponseDTO(reservation);
    }

    private void validateReservationRequest(ReservationRequestDTO reservationRequest) {
        LocalDate startDate = reservationRequest.getStartDate();
        LocalDate endDate = reservationRequest.getEndDate();

        validateStartDateNotAfterEndDate(startDate, endDate);
        validateReservationDuration(startDate, endDate);
        validateReservationStartDateIsBeforeMaxAllowedDate(startDate);
        validateRoomNotReserved(startDate, reservationRequest.getRoomId());
    }

    private void validateRoomNotReserved(LocalDate startDate, Long roomId) {
        var roomAvailability = roomAvailabilityService.getRoomAvailability(startDate, roomId);

        if (!roomAvailability.isAvailable()) {
           throw new RoomAlreadyReservedException();
        }
    }

    private void validateReservationStartDateIsBeforeMaxAllowedDate(LocalDate startDate) {
        LocalDate limitDate = LocalDate.now(clock).plusDays(30);
        if (startDate.isAfter(limitDate)) {
            throw new ReservationAfterAllowedMaximumException();
        }
    }

    private static void validateReservationDuration(LocalDate startDate, LocalDate endDate) {
        if (ChronoUnit.DAYS.between(startDate, endDate) >= MAXIMUM_RESERVATION_DAS_ALLOWED) {
            throw new ReservationTooLongException();
        }
    }

    private static void validateStartDateNotAfterEndDate(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("The start date cannot be after the end date");
        }
    }
}
