package service;

import Model.Book;
import Model.Transaction;
import dao.DAOFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatsService {

    public Map<String, Object> getSystemStats() {
        List<Book> books = DAOFactory.getBookDAO().getAllBooks();
        long physicalCount = books.stream().filter(b -> "PHYSICAL".equalsIgnoreCase(b.getBookType())).count();
        long ebookCount = books.stream().filter(b -> "EBOOK".equalsIgnoreCase(b.getBookType())).count();
        long availableCount = books.stream().filter(Book::isAvailable).count();

        List<Transaction> transactions = DAOFactory.getTransactionDAO().getAllTransactions();
        long activeLoans = transactions.stream()
                .filter(t -> "BORROWED".equalsIgnoreCase(t.getStatus()))
                .count();

        double totalRevenue = transactions.stream()
                .mapToDouble(Transaction::getTotalAmount)
                .sum();

        long totalReservations = DAOFactory.getReservationDAO().getAllReservations().stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()) || "READY".equalsIgnoreCase(r.getStatus()))
                .count();

        long totalUsers = DAOFactory.getUserDAO().getAllUsers().size();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalBooks", books.size());
        stats.put("physicalBooks", physicalCount);
        stats.put("ebooks", ebookCount);
        stats.put("availableBooks", availableCount);
        stats.put("activeLoans", activeLoans);
        stats.put("pendingReservations", totalReservations);
        stats.put("totalUsers", totalUsers);
        stats.put("totalRevenue", totalRevenue);
        stats.put("databaseConnected", DAOFactory.isUseDatabase());

        return stats;
    }
}
