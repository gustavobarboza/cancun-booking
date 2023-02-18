package com.gustavo.cancunbooking.service;

import com.gustavo.cancunbooking.exception.ReservationNotFoundException;
import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import com.gustavo.cancunbooking.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CancellationServiceImpl implements CancellationService {
    private final ReservationRepository reservationRepository;

    public CancellationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if(reservationOpt.isEmpty()) {
            throw new ReservationNotFoundException();
        }

        Reservation reservation = reservationOpt.get();

        if (!ReservationStatusEnum.ACTIVE.equals(reservation.getStatus())) {
            throw new IllegalArgumentException("Reservation is not active, cannot be cancelled");
        }
    }
}
