package dao;

import dao.impl.*;
import util.DBConnection;

/**
 * DAOFactory allows switching seamlessly between In-Memory storage (default, zero setup)
 * and MySQL Database storage.
 *
 * Your partner can simply call:
 *   DAOFactory.setUseDatabase(true);
 * to connect to MySQL!
 */
public class DAOFactory {
    private static boolean useDatabase = false;

    private static BookDAO bookDAO;
    private static UserDAO userDAO;
    private static TransactionDAO transactionDAO;
    private static ReservationDAO reservationDAO;
    private static LibraryDAO libraryDAO;

    static {
        // Initialize with in-memory implementations by default
        initInMemory();
    }

    private static void initInMemory() {
        useDatabase = false;
        bookDAO = new InMemoryBookDAO();
        userDAO = new InMemoryUserDAO();
        transactionDAO = new InMemoryTransactionDAO();
        reservationDAO = new InMemoryReservationDAO();
        libraryDAO = new InMemoryLibraryDAO();
    }

    private static void initDatabase() {
        useDatabase = true;
        bookDAO = new DatabaseBookDAO();
        userDAO = new DatabaseUserDAO();
        transactionDAO = new DatabaseTransactionDAO();
        reservationDAO = new DatabaseReservationDAO();
        libraryDAO = new InMemoryLibraryDAO(); // Libraries work in DB mode too
    }

    public static boolean isUseDatabase() {
        return useDatabase;
    }

    /**
     * Switch storage mode. If true, attempts to verify MySQL connection first.
     * If MySQL is unreachable, falls back gracefully to in-memory mode with a warning.
     */
    public static boolean setUseDatabase(boolean enableDB) {
        if (enableDB) {
            if (DBConnection.isDatabaseAvailable()) {
                initDatabase();
                System.out.println("✅ DAOFactory: Successfully connected to MySQL database!");
                return true;
            } else {
                System.err.println("⚠️ DAOFactory: MySQL is not reachable at " + DBConnection.getUrl() + ". Keeping In-Memory storage.");
                initInMemory();
                return false;
            }
        } else {
            initInMemory();
            System.out.println("ℹ️ DAOFactory: Switched to In-Memory storage mode.");
            return true;
        }
    }

    public static BookDAO getBookDAO() {
        return bookDAO;
    }

    public static UserDAO getUserDAO() {
        return userDAO;
    }

    public static TransactionDAO getTransactionDAO() {
        return transactionDAO;
    }

    public static ReservationDAO getReservationDAO() {
        return reservationDAO;
    }

    public static LibraryDAO getLibraryDAO() {
        return libraryDAO;
    }
}
