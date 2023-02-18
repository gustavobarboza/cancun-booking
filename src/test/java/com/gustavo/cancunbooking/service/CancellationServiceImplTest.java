package com.gustavo.cancunbooking.service;

import com.gustavo.cancunbooking.exception.ReservationNotFoundException;
import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CancellationServiceImplTest {

    CancellationServiceImpl cancellationService;

    @Mock
    private ReservationRepository reservationRepository;

    @BeforeEach
    public void setUp() {
        cancellationService = new CancellationServiceImpl(reservationRepository);
    }

    @Test
    public void shouldNotBeAbleToCancelIfReservationNotFound() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());
        //when
        Throwable throwable = catchThrowable(() -> cancellationService.cancelReservation(1L));
        //then
        then(throwable).isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    public void shouldNotBeAbleToCancelIfReservationIsNotActive() {
        // given
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatusEnum.FINISHED);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        //when
        Throwable throwable = catchThrowable(() -> cancellationService.cancelReservation(1L));
        //then
        then(throwable).isInstanceOf(IllegalArgumentException.class).hasMessage("Reservation is not active, cannot be cancelled");
    }

}