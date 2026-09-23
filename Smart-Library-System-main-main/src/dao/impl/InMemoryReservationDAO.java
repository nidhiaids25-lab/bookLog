package dao.impl;

import Model.Reservation;
import dao.ReservationDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * In-Memory thread-safe implementation of ReservationDAO.
 * Satisfies Day 4: Queue<Reservation> FIFO reservation queue.
 * When a book is returned, pollNextInQueue(bookId) retrieves the next reservation in line.
 */
public class InMemoryReservationDAO implements ReservationDAO {
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private final Map<String, Queue<Reservation>> bookQueues = new ConcurrentHashMap<>();

    public InMemoryReservationDAO() {
        seedSampleReservations();
    }

    private void seedSampleReservations() {
        Reservation r1 = new Reservation(
            "RES-501",
            "ADMIN",
            "B103",
            "Design Patterns: Elements of Reusable Software",
            "2026-09-18",
            "PENDING",
            1
        );
        addReservation(r1);
    }

    @Override
    public synchronized void addReservation(Reservation reservation) {
        if (reservation != null && reservation.getReservationId() != null) {
            String bookId = reservation.getBookId();
            Queue<Reservation> queue = bookQueues.computeIfAbsent(bookId.toUpperCase(), k -> new ConcurrentLinkedQueue<>());
            reservation.setQueuePosition(queue.size() + 1);
            queue.offer(reservation);
            reservations.put(reservation.getReservationId(), reservation);
        }
    }

    @Override
    public Reservation getReservationById(String reservationId) {
        if (reservationId == null) return null;
        return reservations.get(reservationId);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }

    @Override
    public List<Reservation> getReservationsByUserId(String userId) {
        if (userId == null) return Collections.emptyList();
        return reservations.values().stream()
                .filter(r -> userId.equalsIgnoreCase(r.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> getReservationsByBookId(String bookId) {
        if (bookId == null) return Collections.emptyList();
        return reservations.values().stream()
                .filter(r -> bookId.equalsIgnoreCase(r.getBookId()))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized boolean updateReservation(Reservation reservation) {
        if (reservation != null && reservation.getReservationId() != null && reservations.containsKey(reservation.getReservationId())) {
            reservations.put(reservation.getReservationId(), reservation);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean deleteReservation(String reservationId) {
        Reservation removed = reservations.remove(reservationId);
        if (removed != null && removed.getBookId() != null) {
            Queue<Reservation> queue = bookQueues.get(removed.getBookId().toUpperCase());
            if (queue != null) {
                queue.remove(removed);
                int pos = 1;
                for (Reservation r : queue) {
                    r.setQueuePosition(pos++);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Queue<Reservation> getReservationQueue(String bookId) {
        if (bookId == null) return new LinkedList<>();
        Queue<Reservation> queue = bookQueues.get(bookId.toUpperCase());
        return queue != null ? new LinkedList<>(queue) : new LinkedList<>();
    }

    @Override
    public synchronized Reservation pollNextInQueue(String bookId) {
        if (bookId == null) return null;
        Queue<Reservation> queue = bookQueues.get(bookId.toUpperCase());
        if (queue == null || queue.isEmpty()) return null;

        while (!queue.isEmpty()) {
            Reservation next = queue.poll();
            if (next != null && "PENDING".equalsIgnoreCase(next.getStatus())) {
                next.setStatus("READY");
                next.setQueuePosition(0);
                int pos = 1;
                for (Reservation r : queue) {
                    r.setQueuePosition(pos++);
                }
                reservations.put(next.getReservationId(), next);
                return next;
            }
        }
        return null;
    }

    @Override
    public int getQueuePosition(String reservationId) {
        Reservation res = getReservationById(reservationId);
        if (res == null) return -1;
        return res.getQueuePosition();
    }
}
