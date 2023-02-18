package com.gustavo.cancunbooking.service;

import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceImplTest {

    RoomAvailabilityServiceImpl roomAvailabilityService;

    @Mock
    private ReservationRepository reservationRepository;

    @BeforeEach
    public void setUp() {
        roomAvailabilityService = new RoomAvailabilityServiceImpl(reservationRepository);
    }

    @Test
    public void roomShouldBeAvailableIfStartDateIsAfterCurrentReservationEndDate() {
        // given
        LocalDate reservationEndDate = LocalDate.of(2023, 1, 10);
        Reservation reservation = createReservationMockResponse(reservationEndDate);

        long roomId = 1L;
        given(reservationRepository.findByRoomIdAndStatus(roomId, ReservationStatusEnum.ACTIVE))
                .willReturn(Optional.of(reservation));
        
        // when
        LocalDate dateToCheck = LocalDate.of(2023, 1, 15);
        var responseDTO = roomAvailabilityService.getRoomAvailability(dateToCheck, roomId);

        // then
        then(responseDTO.isAvailable()).isTrue();
        then(responseDTO.getClosestAvailableStartDate()).isEqualTo(reservationEndDate.plusDays(1));
    }

    @Test
    public void roomShouldBeAvailableIfNoReservationIsActive() {
        // given
        LocalDate reservationEndDate = LocalDate.of(2023, 1, 10);
        Reservation reservation = createReservationMockResponse(reservationEndDate);

        long roomId = 1L;
        given(reservationRepository.findByRoomIdAndStatus(roomId, ReservationStatusEnum.ACTIVE))
                .willReturn(Optional.of(reservation));

        // when
        LocalDate dateToCheck = LocalDate.of(2023, 1, 9);
        var responseDTO = roomAvailabilityService.getRoomAvailability(dateToCheck, roomId);

        // then
        then(responseDTO.isAvailable()).isFalse();
        then(responseDTO.getClosestAvailableStartDate()).isEqualTo(reservationEndDate.plusDays(1));
    }

    @Test
    public void roomShouldNotBeAvailableIfStartDateIsBeforeCurrentReservationEndDate() {
        // given
        long roomId = 1L;
        given(reservationRepository.findByRoomIdAndStatus(roomId, ReservationStatusEnum.ACTIVE))
                .willReturn(Optional.empty());

        // when
        LocalDate dateToCheck = LocalDate.of(2023, 1, 9);
        var responseDTO = roomAvailabilityService.getRoomAvailability(dateToCheck, roomId);

        // then
        then(responseDTO.isAvailable()).isTrue();
        then(responseDTO.getClosestAvailableStartDate()).isEqualTo(LocalDate.now().plusDays(1));
    }

    private static Reservation createReservationMockResponse(LocalDate reservationEndDate) {
        Reservation reservation = new Reservation();
        reservation.setEndDate(reservationEndDate);
        return reservation;
    }
}