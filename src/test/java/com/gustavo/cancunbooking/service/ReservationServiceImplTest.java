package com.gustavo.cancunbooking.service;

import com.gustavo.cancunbooking.controller.request.ReservationRequestDTO;
import com.gustavo.cancunbooking.controller.response.ReservationSuccessResponseDTO;
import com.gustavo.cancunbooking.controller.response.RoomAvailabilityResponseDTO;
import com.gustavo.cancunbooking.exception.ReservationAfterAllowedMaximumException;
import com.gustavo.cancunbooking.exception.ReservationTooLongException;
import com.gustavo.cancunbooking.exception.RoomAlreadyReservedException;
import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.Room;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.model.User;
import com.gustavo.cancunbooking.repository.ReservationRepository;
import com.gustavo.cancunbooking.repository.RoomRepository;
import com.gustavo.cancunbooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

// TODO decide if the exception types should be changed
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    ReservationServiceImpl reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoomAvailabilityService roomAvailabilityService;
    @BeforeEach
    public void setUp() {
        reservationService = new ReservationServiceImpl(reservationRepository,roomRepository, userRepository, roomAvailabilityService, Clock.systemUTC());
    }

    @Test
    public void shouldNotBeAbleToPlaceAReservationIfStartDateIsAfterEndDate() {
        //
        var startDate = LocalDate.of(2023, 1, 2);
        var endDate = LocalDate.of(2023, 1, 1);
        var request = new ReservationRequestDTO(1L, 1L, startDate, endDate);

        //when
        Throwable thrown = catchThrowable(() -> reservationService.placeReservation(request));

        // then
        then(thrown).isInstanceOf(IllegalArgumentException.class).hasMessage("The start date cannot be after the end date");
    }
    @Test
    public void shouldNotBeAbleToPlaceAReservationLongerThanThreeDays() {
        // given
        var startDate = LocalDate.of(2023, 1, 1);
        var endDate = LocalDate.of(2023, 1, 4);
        var request = new ReservationRequestDTO(1L, 1L, startDate, endDate);

        //when
        Throwable thrown = catchThrowable(() -> reservationService.placeReservation(request));
        // then
        then(thrown).isInstanceOf(ReservationTooLongException.class);
    }

    @Test
    public void shouldNotBeAbleToPlaceAReservationMoreThanThirtyDaysIntoTheFuture() {
        // given
        Clock fixedClock = Clock.fixed(Instant.parse("2023-01-01T10:30:00.00Z"), ZoneId.of("America/Sao_Paulo"));

        reservationService = new ReservationServiceImpl(reservationRepository, roomRepository, userRepository, roomAvailabilityService, fixedClock);

        var startDate = LocalDate.of(2023, 2, 1);
        var endDate = LocalDate.of(2023, 2, 2);
        var request = new ReservationRequestDTO(1L, 1L, startDate, endDate);

        // when
        Throwable thrown = catchThrowable(() -> reservationService.placeReservation(request));

        // then
        then(thrown).isInstanceOf(ReservationAfterAllowedMaximumException.class);
    }

    @Test
    public void shouldNotBeAbleToPlaceAReservationIfTheRoomIsAlreadyReserved() {

        var startDate = LocalDate.of(2023, 1, 5);
        var endDate = LocalDate.of(2023, 1, 7);
        var request = new ReservationRequestDTO(1L, 1L, startDate, endDate);

        var closestAvailableStartDate = LocalDate.of(2023, 1, 8);
        var roomAvailabilityResponse = new RoomAvailabilityResponseDTO(false, closestAvailableStartDate);
        given(roomAvailabilityService.getRoomAvailability(startDate, 1L)).willReturn(roomAvailabilityResponse);

        // when
        Throwable thrown = catchThrowable(() -> reservationService.placeReservation(request));

        // then
        then(thrown).isInstanceOf(RoomAlreadyReservedException.class);
    }

    @Test
    public void shouldBeAbleToPlaceANewReservation() {
        // given
        Clock fixedClock = Clock.fixed(Instant.parse("2023-01-01T10:30:00.00Z"), ZoneId.of("America/Sao_Paulo"));

        reservationService = new ReservationServiceImpl(reservationRepository, roomRepository, userRepository, roomAvailabilityService, fixedClock);

        var startDate = LocalDate.of(2023, 1, 10);
        var endDate = LocalDate.of(2023, 1, 12);
        var request = new ReservationRequestDTO(1L, 1L, startDate, endDate);

        var roomAvailabilityResponse = new RoomAvailabilityResponseDTO(true, null);
        given(roomAvailabilityService.getRoomAvailability(startDate, 1L)).willReturn(roomAvailabilityResponse);

        var room = new Room();
        room.setId(1L);
        given(roomRepository.getReferenceById(1L)).willReturn(room);

        var user = new User();
        user.setId(1L);
        given(userRepository.getReferenceById(1L)).willReturn(user);

        var reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setStatus(ReservationStatusEnum.ACTIVE);
        reservation.setUser(user);
        reservation.setRoom(room);

        given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

        // when
        ReservationSuccessResponseDTO actual = reservationService.placeReservation(request);

        // then
        then(actual.getReservationId()).isNotNull();
        then(actual.getUserId()).isEqualTo(1L);
        then(actual.getRoomId()).isEqualTo(1L);
        then(actual.getStartDate()).isEqualTo(startDate);
        then(actual.getEndDate()).isEqualTo(endDate);
    }
}