package com.gustavo.cancunbooking.services;

import com.gustavo.cancunbooking.controllers.request.ReservationRequestDTO;
import com.gustavo.cancunbooking.controllers.request.ReservationUpdateRequestDTO;
import com.gustavo.cancunbooking.controllers.response.ReservationSuccessResponseDTO;
import com.gustavo.cancunbooking.exceptions.*;
import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.model.Room;
import com.gustavo.cancunbooking.model.User;
import com.gustavo.cancunbooking.repositories.ReservationRepository;
import com.gustavo.cancunbooking.repositories.RoomRepository;
import com.gustavo.cancunbooking.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class ReservationServiceImpl implements ReservationService {

    public static final int MAXIMUM_RESERVATION_DAS_ALLOWED = 3;
    public static final int MAXIMUM_FUTURE_DAYS_ALLOWED = 30;

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
        validateNewReservationRequest(reservationRequest);

        Reservation reservation = new Reservation();
        reservation.setStartDate(reservationRequest.getStartDate());
        reservation.setEndDate(reservationRequest.getEndDate());
        reservation.setStatus(ReservationStatusEnum.ACTIVE);

        Room room = roomRepository.findById(reservationRequest.getRoomId())
                .orElseThrow(() -> new ReservationException("No room found with the provided id"));
        reservation.setRoom(room);

        User user = userRepository.findById(reservationRequest.getUserId())
                .orElseThrow(() -> new ReservationException("No user found with the provided id"));
        reservation.setUser(user);

        reservationRepository.save(reservation);
        return new ReservationSuccessResponseDTO(reservation);
    }

    @Override
    public ReservationSuccessResponseDTO updateReservation(ReservationUpdateRequestDTO reservationUpdateRequest) {
        Reservation reservation = getReservation(reservationUpdateRequest.getReservationId());
        validateReservationUpdateRequest(reservationUpdateRequest, reservation);

        reservation.setStartDate(reservationUpdateRequest.getStartDate());
        reservation.setEndDate(reservationUpdateRequest.getEndDate());

        reservationRepository.save(reservation);
        return new ReservationSuccessResponseDTO(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = getReservation(reservationId);

        validateReservationStatusIsActive(reservation.getStatus());

        reservation.setStatus(ReservationStatusEnum.CANCELLED);
        reservationRepository.save(reservation);
    }

    private Reservation getReservation(Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        return reservationOpt.orElseThrow(
                () -> new ReservationException("No reservation found with the given id"));
    }

    private void validateNewReservationRequest(ReservationRequestDTO reservationRequest) {
        LocalDate startDate = reservationRequest.getStartDate();
        LocalDate endDate = reservationRequest.getEndDate();

        validateStartDateAndEndDateAreNotEqual(startDate, endDate);
        validateStartDateNotToday(startDate);
        validateStartDateNotAfterEndDate(startDate, endDate);
        validateReservationDuration(startDate, endDate);
        validateReservationNotAfterMaximumAllowedStartDate(startDate);
        validateRoomNotReserved(startDate, reservationRequest.getRoomId());
    }

    private void validateReservationUpdateRequest(ReservationUpdateRequestDTO reservationUpdateRequest, Reservation reservation) {
        validateReservationStatusIsActive(reservation.getStatus());

        LocalDate updateRequestStartDate = reservationUpdateRequest.getStartDate();
        LocalDate updateRequestEndDate = reservationUpdateRequest.getEndDate();

        validateStartDateAndEndDateAreNotEqual(updateRequestStartDate, updateRequestEndDate);
        validateStartDateNotToday(updateRequestStartDate);
        validateStartDateNotAfterEndDate(updateRequestStartDate, updateRequestEndDate);
        validateReservationDuration(updateRequestStartDate, updateRequestEndDate);
        validateReservationNotAfterMaximumAllowedStartDate(updateRequestStartDate);
        validateRoomNotReserved(updateRequestStartDate, reservation.getRoom().getId());
    }

    private static void validateReservationStatusIsActive(ReservationStatusEnum status) {
        if (!ReservationStatusEnum.ACTIVE.equals(status)) {
            throw new ReservationException("Reservation must be active");
        }
    }

    private void validateRoomNotReserved(LocalDate startDate, Long roomId) {
        var roomAvailability = roomAvailabilityService.getRoomAvailability(startDate, roomId);

        if (!roomAvailability.isAvailable()) {
           throw new ReservationException("Room is already reserved in the provided period");
        }
    }

    private void validateReservationNotAfterMaximumAllowedStartDate(LocalDate startDate) {
        LocalDate limitDate = LocalDate.now(clock).plusDays(MAXIMUM_FUTURE_DAYS_ALLOWED);
        if (startDate.isAfter(limitDate)) {
            throw new ReservationException("Reservation cannot be more than 30 days into the future");
        }
    }

    private static void validateReservationDuration(LocalDate startDate, LocalDate endDate) {
        if (ChronoUnit.DAYS.between(startDate, endDate) >= MAXIMUM_RESERVATION_DAS_ALLOWED) {
            throw new ReservationException("Reservation period cannot be greater than 3 days");
        }
    }

    private static void validateStartDateNotAfterEndDate(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ReservationException("The start date cannot be after the end date");
        }
    }

    private void validateStartDateAndEndDateAreNotEqual(LocalDate startDate, LocalDate endDate) {
        if (startDate.equals(endDate)) {
            throw new ReservationException("The start and end date cannot be the same");
        }
    }

    private void validateStartDateNotToday(LocalDate startDate) {
        if (LocalDate.now(clock).isEqual(startDate)) {
            throw new ReservationException("Reservation cannot start today");
        }
    }
}
