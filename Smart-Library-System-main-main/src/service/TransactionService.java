package service;

import Model.Book;
import Model.Borrowable;
import Model.Reservation;
import Model.Transaction;
import Model.TransactionType;
import dao.BookDAO;
import dao.DAOFactory;
import dao.ReservationDAO;
import dao.TransactionDAO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final BookDAO bookDAO;
    private final ReservationDAO reservationDAO;

    public TransactionService() {
        this.transactionDAO = DAOFactory.getTransactionDAO();
        this.bookDAO = DAOFactory.getBookDAO();
        this.reservationDAO = DAOFactory.getReservationDAO();
    }

    public TransactionService(TransactionDAO transactionDAO, BookDAO bookDAO, ReservationDAO reservationDAO) {
        this.transactionDAO = transactionDAO;
        this.bookDAO = bookDAO;
        this.reservationDAO = reservationDAO;
    }

    /**
     * Borrow a book for a given number of days.
     * Uses OOP Polymorphism to determine rental price.
     */
    public Transaction borrowBook(String userId, String bookId, int days) {
        if (userId == null || bookId == null) {
            System.err.println("❌ User ID and Book ID are required to borrow.");
            return null;
        }

        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            System.err.println("❌ Book not found: " + bookId);
            return null;
        }

        if (!book.isAvailable()) {
            System.err.println("❌ Book is currently unavailable: " + book.getTitle());
            return null;
        }

        int loanDays = Math.max(1, days);
        double rentalPrice = book.getRentalPrice(loanDays);

        // Polymorphism: invoke interface method
        if (book instanceof Borrowable) {
            ((Borrowable) book).borrowBook();
        } else {
            book.setAvailable(false);
        }
        bookDAO.updateBook(book);

        String txId = "TX-" + (System.currentTimeMillis() % 100000);
        LocalDate now = LocalDate.now();
        LocalDate due = now.plusDays(loanDays);

        Transaction tx = new Transaction(
            txId,
            userId,
            bookId,
            book.getTitle(),
            now.toString(),
            due.toString(),
            null,
            rentalPrice,
            0.0,
            "BORROWED"
        );

        transactionDAO.addTransaction(tx);
        System.out.println("✅ Book '" + book.getTitle() + "' borrowed by " + userId + ". Rental: ₹" + rentalPrice);
        return tx;
    }

    /**
     * Buy a book permanently (Day 4: Buy Flow).
     * Deducts available copy count and creates a PURCHASED transaction.
     */
    public Transaction buyBook(String userId, String bookId) {
        if (userId == null || bookId == null) {
            System.err.println("❌ User ID and Book ID are required to purchase.");
            return null;
        }

        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            System.err.println("❌ Book not found: " + bookId);
            return null;
        }

        if (book.getAvailableCopies() <= 0 && !book.isAvailable()) {
            System.err.println("❌ Book is out of stock for purchase: " + book.getTitle());
            return null;
        }

        // Decrement copies
        int remaining = Math.max(0, book.getAvailableCopies() - 1);
        book.setAvailableCopies(remaining);
        if (remaining == 0) {
            book.setAvailable(false);
        }
        bookDAO.updateBook(book);

        String txId = "TX-BUY-" + (System.currentTimeMillis() % 100000);
        LocalDate now = LocalDate.now();
        double purchasePrice = book.getBuyPrice();

        Transaction tx = new Transaction(
            txId,
            userId,
            bookId,
            book.getTitle(),
            TransactionType.BUY,
            now.toString(),
            null, // No due date
            null, // No return date
            purchasePrice,
            0.0,
            "PURCHASED"
        );

        transactionDAO.addTransaction(tx);
        System.out.println("🎉 Book '" + book.getTitle() + "' purchased by " + userId + " for ₹" + purchasePrice);
        return tx;
    }

    /**
     * Return a borrowed book with automatic late fine calculation & reservation notification.
     */
    public Transaction returnBook(String transactionId) {
        Map<String, Object> details = returnBookWithDetails(transactionId);
        if (details != null && details.containsKey("transaction")) {
            return (Transaction) details.get("transaction");
        }
        return null;
    }

    public Map<String, Object> returnBookWithDetails(String transactionId) {
        if (transactionId == null) return null;

        Transaction tx = transactionDAO.getTransactionById(transactionId);
        if (tx == null) {
            System.err.println("❌ Transaction not found: " + transactionId);
            return null;
        }

        Map<String, Object> receipt = new LinkedHashMap<>();

        if ("RETURNED".equalsIgnoreCase(tx.getStatus())) {
            receipt.put("transaction", tx);
            receipt.put("alreadyReturned", true);
            receipt.put("message", "Book was already returned previously.");
            return receipt;
        }

        LocalDate returnDate = LocalDate.now();
        tx.setReturnDate(returnDate.toString());
        tx.setStatus("RETURNED");

        // Calculate late fine (₹15 per day overdue)
        double fine = 0.0;
        long daysLate = 0;
        try {
            if (tx.getDueDate() != null && !tx.getDueDate().isEmpty()) {
                LocalDate dueDate = LocalDate.parse(tx.getDueDate());
                if (returnDate.isAfter(dueDate)) {
                    daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
                    fine = daysLate * 15.0; // ₹15/day late fee
                }
            }
        } catch (Exception ignored) {}

        tx.setOverdueFine(fine);
        transactionDAO.updateTransaction(tx);

        // Mark book available again
        Book book = bookDAO.getBookById(tx.getBookId());
        String reservationNotice = null;
        if (book != null) {
            if (book instanceof Borrowable) {
                ((Borrowable) book).returnBook();
            } else {
                book.setAvailable(true);
            }
            bookDAO.updateBook(book);

            // FIFO Reservation Queue (Day 4 Requirement): Next user in queue gets the returned book
            Reservation nextInQueue = reservationDAO.pollNextInQueue(book.getBookId());
            if (nextInQueue != null) {
                reservationNotice = "FIFO Queue Allocation: Next reader in queue '" + nextInQueue.getUserId() +
                                    "' has been allocated this book (Reservation #" + nextInQueue.getReservationId() + "). Status: READY.";
                System.out.println("🔔 " + reservationNotice);
                receipt.put("nextReservation", nextInQueue.toMap());
            }
        }

        receipt.put("transaction", tx);
        receipt.put("daysLate", daysLate);
        receipt.put("overdueFine", fine);
        receipt.put("totalPaid", tx.getTotalAmount());
        receipt.put("reservationAlert", reservationNotice);
        receipt.put("message", "Book returned successfully!");
        return receipt;
    }

    public List<Transaction> getUserTransactions(String userId) {
        return transactionDAO.getTransactionsByUserId(userId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }

    public Transaction getTransaction(String transactionId) {
        return transactionDAO.getTransactionById(transactionId);
    }
}
