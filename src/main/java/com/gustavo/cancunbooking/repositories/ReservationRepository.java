package com.gustavo.cancunbooking.repositories;

import com.gustavo.cancunbooking.model.Reservation;
import com.gustavo.cancunbooking.model.ReservationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("Select r From Reservation r " +
            "where r.endDate >= :date " +
            "and r.room.id = :roomId " +
            "and r.status = com.gustavo.cancunbooking.model.ReservationStatusEnum.ACTIVE")
    Optional<Reservation> findReservationAtDateOrGreater(LocalDate date, Long roomId);

    @Query("Select r From Reservation r " +
            "where r.id = :roomId " +
            "and r.status = :status")
    Optional<Reservation> findByRoomIdAndStatus(Long roomId, ReservationStatusEnum status);
}
