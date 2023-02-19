package com.gustavo.cancunbooking.services;

import com.gustavo.cancunbooking.controllers.request.ReservationRequestDTO;
import com.gustavo.cancunbooking.controllers.request.ReservationUpdateRequestDTO;
import com.gustavo.cancunbooking.controllers.response.ReservationSuccessResponseDTO;
import com.gustavo.cancunbooking.controllers.response.RoomAvailabilityResponseDTO;
import com.gustavo.cancunbooking.exceptions.ReservationException;
import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.model.Room;
import com.gustavo.cancunbooking.model.User;
import com.gustavo.cancunbooking.repositories.ReservationRepository;
import com.gustavo.cancunbooking.repositories.RoomRepository;
import com.gustavo.cancunbooking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

    @Captor
    ArgumentCaptor<Reservation> reservationCaptor;
    @BeforeEach
    public void setUp() {
        reservationService = new ReservationServiceImpl(reservationRepository,roomRepository, userRepository, roomAvailabilityService, Clock.systemUTC());
    }

    @Test
    public void shouldNotBeAbleToPlaceAReservationIfStartDateIsAfterEndDate() {
        // given
        var startDate = LocalDate.of(2023, 1, 2);
        var endDate = LocalDate.of(2023, 1, 1);
        ReservationRequestDTO request = createReservationRequest(startDate, endDate);

        //when
        Throwable thrown = catchThrowable(() -> reservationService.placeReservation(request));

        // then
        then(thrown).isInstanceOf(ReservationException.class).hasMessage("The start date cannot be after the end date");
    }

    @Test
    public void shouldNotBeAbleToPlaceAReservationLongerThanThreeDays() {
        // given
        var startDate = LocalDate.of(2023, 1, 1);
        var endDate = LocalDate.of(2023, 1, 4);
        ReservationRequestDTO request = createReservationRequest(startDate, endDate);

        //when
        Throwable thrown = catchThrowable(() -> reservationService.placeReservation(request));

        // then
        then(thrown).isInstanceOf(ReservationException.class).hasMessage("Reservation period cannot be greater than 3 days");
    }

    @Test
    public void shouldNotBeAbleToPlaceAReservationMoreThanThirtyDaysIntoTheFuture() {
        // given
        var clockDateTime = "2023-01-01T10:30:00.00Z";
        Clock fixedClock = createFixedClock(clockDateTime);

        reservationService = new ReservationServiceImpl(reservationRepository, roomRepository, userRepository, roomAvailabilityService, fixedClock);

        var startDate = LocalDate.of(2023, 2, 1);
        var endDate = LocalDate.of(2023, 2, 2);
        ReservationRequestDTO request = createReservationRequest(startDate, endDate);

        // when
        Throwable thrown = catchThrowable(() -> reservationService.placeReservation(request));

        // then
        then(thrown).isInstanceOf(ReservationException.class).hasMessage("Reservation cannot be more than 30 days into the future");
    }

    @Test
    public void shouldNotBeAbleToPlaceAReservationIfTheRoomIsAlreadyReserved() {

        var startDate = LocalDate.of(2023, 1, 5);
        var endDate = LocalDate.of(2023, 1, 7);
        ReservationRequestDTO request = createReservationRequest(startDate, endDate);

        var closestAvailableStartDate = LocalDate.of(2023, 1, 8);
        var roomAvailabilityResponse = new RoomAvailabilityResponseDTO(false, closestAvailableStartDate);
        given(roomAvailabilityService.getRoomAvailability(startDate, 1L)).willReturn(roomAvailabilityResponse);

        // when
        Throwable thrown = catchThrowable(() -> reservationService.placeReservation(request));

        // then
        then(thrown).isInstanceOf(ReservationException.class).hasMessage("Room is already reserved in the provided period");
    }

    @Test
    public void shouldBeAbleToPlaceANewReservation() {
        // given
        Clock fixedClock = createFixedClock("2023-01-01T10:30:00.00Z");

        reservationService = new ReservationServiceImpl(reservationRepository, roomRepository, userRepository, roomAvailabilityService, fixedClock);

        var startDate = LocalDate.of(2023, 1, 10);
        var endDate = LocalDate.of(2023, 1, 12);
        ReservationRequestDTO request = createReservationRequest(startDate, endDate);

        var roomAvailabilityResponse = new RoomAvailabilityResponseDTO(true, null);
        given(roomAvailabilityService.getRoomAvailability(startDate, 1L)).willReturn(roomAvailabilityResponse);

        var room = new Room();
        room.setId(1L);
        given(roomRepository.getReferenceById(1L)).willReturn(room);

        var user = new User();
        user.setId(1L);
        given(userRepository.getReferenceById(1L)).willReturn(user);

        // when
        ReservationSuccessResponseDTO actual = reservationService.placeReservation(request);

        // then
        verify(reservationRepository).save(any(Reservation.class));
        then(actual.getUserId()).isEqualTo(1L);
        then(actual.getRoomId()).isEqualTo(1L);
        then(actual.getStartDate()).isEqualTo(startDate);
        then(actual.getEndDate()).isEqualTo(endDate);
    }

    @Test
    public void shouldNotBeAbleToUpdateAReservationIfItDoesNotExist() {
        //given
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());
        ReservationUpdateRequestDTO reservationUpdateRequest = createReservationUpdateRequest(1L, null, null);

        // when
        Throwable throwable = catchThrowable(() -> reservationService.updateReservation(reservationUpdateRequest));

        // then
        then(throwable).isInstanceOf(ReservationException.class).hasMessage("No reservation found with the given id");
    }

    @Test
    public void shouldNotBeAbleToUpdateAReservationIfItIsNotActive() {
        // given
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatusEnum.FINISHED);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        ReservationUpdateRequestDTO reservationUpdateRequest = createReservationUpdateRequest(1L, null, null);

        //when
        Throwable throwable = catchThrowable(() -> reservationService.updateReservation(reservationUpdateRequest));

        //then
        then(throwable).isInstanceOf(ReservationException.class).hasMessage("Reservation must be active");
    }

    @Test
    public void shouldNotBeAbleToUpdateAReservationLWithAPeriodGreaterThanAllowed() {
        // given
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatusEnum.ACTIVE);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        var startDate = LocalDate.of(2023, 1, 1);
        var endDate = LocalDate.of(2023, 1, 4);
        ReservationUpdateRequestDTO request = createReservationUpdateRequest(1L, startDate, endDate);

        //when
        Throwable thrown = catchThrowable(() -> reservationService.updateReservation(request));
        // then
        then(thrown).isInstanceOf(ReservationException.class).hasMessage("Reservation period cannot be greater than 3 days");
    }

    @Test
    public void shouldNotBeAbleToUpdateAReservationMoreThanThirtyDaysIntoTheFuture() {
        // given
        Clock fixedClock = createFixedClock("2023-01-01T10:30:00.00Z");

        reservationService = new ReservationServiceImpl(reservationRepository, roomRepository, userRepository, roomAvailabilityService, fixedClock);


        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatusEnum.ACTIVE);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        var startDate = LocalDate.of(2023, 2, 1);
        var endDate = LocalDate.of(2023, 2, 2);
        ReservationUpdateRequestDTO request = createReservationUpdateRequest(1L, startDate, endDate);

        // when
        Throwable thrown = catchThrowable(() -> reservationService.updateReservation(request));

        // then
        then(thrown).isInstanceOf(ReservationException.class).hasMessage("Reservation cannot be more than 30 days into the future");
    }

    // cannot update a reservation whose start date and end dates clash with another reservation
    @Test
    public void shouldNotBeAbleToUpdateAReservationIfTheRoomIsAlreadyReserved() {

        var reservation = new Reservation();
        reservation.setStatus(ReservationStatusEnum.ACTIVE);

        var room = new Room();
        room.setId(1L);
        reservation.setRoom(room);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        var startDate = LocalDate.of(2023, 2, 1);
        var endDate = LocalDate.of(2023, 2, 2);
        ReservationUpdateRequestDTO request = createReservationUpdateRequest(1L, startDate, endDate);

        var roomAvailabilityResponse = new RoomAvailabilityResponseDTO(false, null);
        given(roomAvailabilityService.getRoomAvailability(startDate, 1L)).willReturn(roomAvailabilityResponse);

        // when
        Throwable thrown = catchThrowable(() -> reservationService.updateReservation(request));

        // then
        then(thrown).isInstanceOf(ReservationException.class).hasMessage("Room is already reserved in the provided period");
    }

    // can update a reservation if input is valid.
    @Test
    public void shouldBeAbleToUpdateAReservationIfInputIsValid() {
        Clock fixedClock = createFixedClock("2023-01-01T10:30:00.00Z");

        reservationService = new ReservationServiceImpl(reservationRepository, roomRepository, userRepository, roomAvailabilityService, fixedClock);

        var startDate = LocalDate.of(2023, 1, 10);
        var endDate = LocalDate.of(2023, 1, 12);
        ReservationUpdateRequestDTO request = createReservationUpdateRequest(1L, startDate, endDate);

        var roomAvailabilityResponse = new RoomAvailabilityResponseDTO(true, null);
        given(roomAvailabilityService.getRoomAvailability(startDate, 1L)).willReturn(roomAvailabilityResponse);

        var room = new Room();
        room.setId(1L);
        var user = new User();
        user.setId(1L);

        var oldReservation = new Reservation();
        oldReservation.setStatus(ReservationStatusEnum.ACTIVE);
        oldReservation.setRoom(room);
        oldReservation.setUser(user);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(oldReservation));


        // when
        ReservationSuccessResponseDTO actual = reservationService.updateReservation(request);

        //then
        verify(reservationRepository).save(any(Reservation.class));
        then(actual.getUserId()).isEqualTo(1L);
        then(actual.getRoomId()).isEqualTo(1L);
        then(actual.getStartDate()).isEqualTo(startDate);
        then(actual.getEndDate()).isEqualTo(endDate);
    }

    @Test
    public void shouldNotBeAbleToCancelAReservationIfItDoesNotExist() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());
        //when
        Throwable throwable = catchThrowable(() -> reservationService.cancelReservation(1L));
        //then
        then(throwable).isInstanceOf(ReservationException.class).hasMessage("No reservation found with the given id");
    }

    @Test
    public void shouldNotBeAbleToCancelAReservationIfItIsNotActive() {
        // given
        var reservation = new Reservation();
        reservation.setStatus(ReservationStatusEnum.FINISHED);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        //when
        Throwable throwable = catchThrowable(() -> reservationService.cancelReservation(1L));

        //then
        then(throwable).isInstanceOf(ReservationException.class).hasMessage("Reservation must be active");
    }

    @Test
    public void shouldBeAbleToCancelAnActiveReservation() {
        //given
        var reservation = new Reservation();
        reservation.setStatus(ReservationStatusEnum.ACTIVE);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        //when
        reservationService.cancelReservation(1L);

        //then
        verify(reservationRepository).save(reservationCaptor.capture());
        Reservation captured = reservationCaptor.getValue();
        then(captured.getStatus()).isEqualTo(ReservationStatusEnum.CANCELLED);
    }

    private static ReservationRequestDTO createReservationRequest(LocalDate startDate, LocalDate endDate) {
        return new ReservationRequestDTO(1L, 1L, startDate, endDate);
    }

    private static ReservationUpdateRequestDTO createReservationUpdateRequest(Long reservationId, LocalDate startDate, LocalDate endDate) {
        return new ReservationUpdateRequestDTO(reservationId, startDate, endDate);
    }

    private static Clock createFixedClock(String clockDateTime) {
        return Clock.fixed(Instant.parse(clockDateTime), ZoneId.of("America/Sao_Paulo"));
    }
}