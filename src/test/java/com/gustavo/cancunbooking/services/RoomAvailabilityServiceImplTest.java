package com.gustavo.cancunbooking.services;

import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.repositories.ReservationRepository;
import com.gustavo.cancunbooking.repositories.RoomRepository;
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

    @Mock
    private RoomRepository roomRepository;

    @BeforeEach
    public void setUp() {
        roomAvailabilityService = new RoomAvailabilityServiceImpl(reservationRepository, roomRepository);
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
    public void roomShouldNotBeAvailableIfStartDateIsBeforeCurrentReservationEndDate() {
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
    public void roomShouldBeAvailableIfNoReservationIsActive() {
        // given
        long roomId = 1L;
        given(reservationRepository.findByRoomIdAndStatus(roomId, ReservationStatusEnum.ACTIVE))
                .willReturn(Optional.empty());
        given(roomRepository.existsById(roomId)).willReturn(true);

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