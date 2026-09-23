package service;

import Model.Book;
import Model.Reservation;
import dao.BookDAO;
import dao.DAOFactory;
import dao.ReservationDAO;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {
    private final ReservationDAO reservationDAO;
    private final BookDAO bookDAO;

    public ReservationService() {
        this.reservationDAO = DAOFactory.getReservationDAO();
        this.bookDAO = DAOFactory.getBookDAO();
    }

    public ReservationService(ReservationDAO reservationDAO, BookDAO bookDAO) {
        this.reservationDAO = reservationDAO;
        this.bookDAO = bookDAO;
    }

    public Reservation reserveBook(String userId, String bookId) {
        if (userId == null || bookId == null) {
            System.err.println("❌ User ID and Book ID are required to reserve.");
            return null;
        }

        Book book = bookDAO.getBookById(bookId);
        String bookTitle = (book != null) ? book.getTitle() : "Unknown Title";

        String resId = "RES-" + (System.currentTimeMillis() % 100000);
        Reservation res = new Reservation(
            resId,
            userId,
            bookId,
            bookTitle,
            LocalDate.now().toString(),
            "PENDING"
        );

        reservationDAO.addReservation(res);
        System.out.println("✅ Reservation created: " + resId + " for user " + userId + " on book '" + bookTitle + "'");
        return res;
    }

    public boolean cancelReservation(String reservationId) {
        Reservation res = reservationDAO.getReservationById(reservationId);
        if (res != null) {
            res.setStatus("CANCELLED");
            reservationDAO.updateReservation(res);
            return true;
        }
        return false;
    }

    public boolean fulfillReservation(String reservationId) {
        Reservation res = reservationDAO.getReservationById(reservationId);
        if (res != null) {
            res.setStatus("FULFILLED");
            reservationDAO.updateReservation(res);
            return true;
        }
        return false;
    }

    public Reservation getReservation(String reservationId) {
        return reservationDAO.getReservationById(reservationId);
    }

    public List<Reservation> getUserReservations(String userId) {
        return reservationDAO.getReservationsByUserId(userId);
    }

    public List<Reservation> getAllReservations() {
        return reservationDAO.getAllReservations();
    }

    public List<Reservation> getBookQueue(String bookId) {
        return new java.util.ArrayList<>(reservationDAO.getReservationQueue(bookId));
    }

    public Reservation pollNextReservation(String bookId) {
        return reservationDAO.pollNextInQueue(bookId);
    }
}
