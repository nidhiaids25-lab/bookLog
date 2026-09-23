package dao;

import Model.Reservation;
import java.util.List;
import java.util.Queue;

/**
 * Interface defining Reservation Data Access operations.
 * Satisfies Day 4: FIFO Reservation Queue (Queue<Reservation>).
 */
public interface ReservationDAO {
    void addReservation(Reservation reservation);
    Reservation getReservationById(String reservationId);
    List<Reservation> getAllReservations();
    List<Reservation> getReservationsByUserId(String userId);
    List<Reservation> getReservationsByBookId(String bookId);
    boolean updateReservation(Reservation reservation);
    boolean deleteReservation(String reservationId);

    // FIFO Queue Operations
    Queue<Reservation> getReservationQueue(String bookId);
    Reservation pollNextInQueue(String bookId);
    int getQueuePosition(String reservationId);
}
