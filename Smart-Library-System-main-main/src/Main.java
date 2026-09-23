import Model.*;
import server.HttpServerApp;
import service.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("==================================================================");
        System.out.println("         📚 SMART LIBRARY SYSTEM - SERVER & BACKEND               ");
        System.out.println("==================================================================");

        // 1. Start Web Server
        HttpServerApp server = new HttpServerApp(port);
        try {
            server.start();
        } catch (Exception e) {
            System.err.println("❌ Failed to start web server on port " + port + ": " + e.getMessage());
        }

        // 2. Automated Test Verification (Preserving and enhancing original tests)
        System.out.println("\n=== [1] TESTING USER SERVICE (REGISTER & LOGIN) ===");
        UserService userService = new UserService();
        userService.register("U101", "Nidhi", "pass123", "READER");
        userService.register("U102", "Rahul", "seller123", "SELLER");

        User loggedInUser = userService.login("U101", "pass123");
        if (loggedInUser != null) {
            if ("SELLER".equals(loggedInUser.getRole())) {
                System.out.println("Action: You can ADD, UPDATE, or DELETE books.");
            } else if ("READER".equals(loggedInUser.getRole())) {
                System.out.println("Action: You can SEARCH, BORROW, or RESERVE books.");
            }
        }

        // 3. Testing OOP Polymorphism in Rental Pricing
        System.out.println("\n=== [2] TESTING OOP POLYMORPHISM (RENTAL CALCULATION) ===");
        BookService bookService = new BookService();
        Book physical = bookService.getBookById("B101");
        Book ebook = bookService.getBookById("E201");

        if (physical != null) {
            System.out.println("📘 Physical Book: " + physical.getTitle());
            System.out.println("   Rental for 7 days (₹20/day) = ₹" + physical.getRentalPrice(7));
        }
        if (ebook != null) {
            System.out.println("⚡ E-Book: " + ebook.getTitle());
            System.out.println("   Rental for 7 days (₹10/day) = ₹" + ebook.getRentalPrice(7));
        }

        // 4. Testing Borrow & Return workflow
        System.out.println("\n=== [3] TESTING TRANSACTION SERVICE (BORROW & RETURN) ===");
        TransactionService txService = new TransactionService();
        Transaction tx = txService.borrowBook("U101", "B102", 5);
        if (tx != null) {
            System.out.println("   Issued loan: " + tx.getTransactionId() + " (Fee: ₹" + tx.getRentalPrice() + ")");
            // Return it
            txService.returnBook(tx.getTransactionId());
        }

        System.out.println("\n==================================================================");
        System.out.println("  🌐 WEB APPLICATION READY AT: http://localhost:" + port);
        System.out.println("  📡 REST API BASE ENDPOINT:  http://localhost:" + port + "/api");
        System.out.println("  📖 BACKEND GUIDE:           BACKEND_GUIDE.md (For Partner)");
        System.out.println("==================================================================");
        System.out.println("Commands: type 'status', 'books', 'users', or 'exit' (or press Ctrl+C)\n");

        // Keep server running and accept console commands
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                System.out.println("Shutting down Smart Library System...");
                server.stop();
                break;
            } else if ("status".equalsIgnoreCase(line)) {
                StatsService statsService = new StatsService();
                System.out.println("System Stats: " + statsService.getSystemStats());
            } else if ("books".equalsIgnoreCase(line)) {
                System.out.println("Total Catalog Books: " + bookService.getAllBooks().size());
                for (Book b : bookService.getAllBooks()) {
                    System.out.println(" - [" + b.getBookType() + "] " + b.getBookId() + ": " + b.getTitle() + " (" + (b.isAvailable() ? "Available" : "Loaned") + ")");
                }
            } else if ("users".equalsIgnoreCase(line)) {
                System.out.println("Registered Users: " + userService.getAllUsers().size());
                for (User u : userService.getAllUsers()) {
                    System.out.println(" - " + u.getUserId() + " (" + u.getName() + ") [" + u.getRole() + "]");
                }
            } else if ("help".equalsIgnoreCase(line)) {
                System.out.println("Available commands: status, books, users, exit");
            } else if (!line.isEmpty()) {
                System.out.println("Unknown command: '" + line + "'. Type 'help' for options.");
            }
        }
        scanner.close();
    }
}
