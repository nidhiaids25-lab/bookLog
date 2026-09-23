package ui;

import Model.Book;
import Model.PhysicalBook;
import Model.EBook;
import Model.User;
import service.BookService;
import service.TransactionService;
import service.UserService;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive Command-Line Interface (CLI UI) for Smart Library System.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BookService bookService = new BookService();
        UserService userService = new UserService();
        TransactionService txService = new TransactionService();

        System.out.println("=================================================");
        System.out.println("       📚 SMART LIBRARY SYSTEM - CLI INTERFACE   ");
        System.out.println("=================================================");

        User currentUser = null;

        while (true) {
            if (currentUser == null) {
                System.out.println("\n--- MAIN MENU ---");
                System.out.println("1. Login (Demo: U101 / pass123)");
                System.out.println("2. Register New User");
                System.out.println("3. Quick Guest Access (as Nidhi)");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();
                if ("1".equals(choice)) {
                    System.out.print("User ID: ");
                    String uid = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String pwd = scanner.nextLine().trim();
                    currentUser = userService.login(uid, pwd);
                } else if ("2".equals(choice)) {
                    System.out.print("User ID: ");
                    String uid = scanner.nextLine().trim();
                    System.out.print("Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String pwd = scanner.nextLine().trim();
                    System.out.print("Role (READER/SELLER): ");
                    String role = scanner.nextLine().trim();
                    currentUser = userService.registerUser(uid, name, pwd, role);
                } else if ("3".equals(choice)) {
                    currentUser = userService.getUser("U101");
                    System.out.println("Logged in as: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
                } else if ("4".equals(choice)) {
                    System.out.println("Goodbye!");
                    break;
                }
            } else {
                System.out.println("\n--- DASHBOARD (" + currentUser.getName() + " - " + currentUser.getRole() + ") ---");
                System.out.println("1. Browse All Books");
                System.out.println("2. Search Books");
                System.out.println("3. Borrow a Book");
                System.out.println("4. Return a Book");
                if ("SELLER".equalsIgnoreCase(currentUser.getRole())) {
                    System.out.println("5. Add New Book (Seller Action)");
                    System.out.println("6. Delete a Book (Seller Action)");
                }
                System.out.println("0. Logout");
                System.out.print("Choose an option: ");

                String option = scanner.nextLine().trim();
                if ("1".equals(option)) {
                    List<Book> books = bookService.getAllBooks();
                    System.out.println("\n--- CATALOG (" + books.size() + " Books) ---");
                    for (Book b : books) {
                        System.out.println(String.format("• [%s] %s | Title: %s | Author: %s | ₹%.0f/day | %s",
                                b.getBookType(), b.getBookId(), b.getTitle(), b.getAuthor(),
                                b.getRentalPrice(1), (b.isAvailable() ? "AVAILABLE" : "ON LOAN")));
                    }
                } else if ("2".equals(option)) {
                    System.out.print("Enter search keyword: ");
                    String q = scanner.nextLine().trim();
                    List<Book> results = bookService.searchBooks(q);
                    System.out.println("Found " + results.size() + " matches.");
                    for (Book b : results) {
                        System.out.println(" - " + b.getBookId() + ": " + b.getTitle() + " (" + b.getBookType() + ")");
                    }
                } else if ("3".equals(option)) {
                    System.out.print("Enter Book ID to borrow (e.g. B101, E201): ");
                    String bid = scanner.nextLine().trim();
                    System.out.print("Loan duration in days: ");
                    int days = 7;
                    try { days = Integer.parseInt(scanner.nextLine().trim()); } catch (Exception ignored) {}
                    txService.borrowBook(currentUser.getUserId(), bid, days);
                } else if ("4".equals(option)) {
                    System.out.print("Enter Transaction ID to return (e.g. TX-1001): ");
                    String txId = scanner.nextLine().trim();
                    txService.returnBook(txId);
                } else if ("5".equals(option) && "SELLER".equalsIgnoreCase(currentUser.getRole())) {
                    System.out.print("Book ID: ");
                    String bid = scanner.nextLine().trim();
                    System.out.print("Title: ");
                    String title = scanner.nextLine().trim();
                    System.out.print("Author: ");
                    String author = scanner.nextLine().trim();
                    System.out.print("Format (1: PHYSICAL, 2: EBOOK): ");
                    String fmt = scanner.nextLine().trim();
                    if ("2".equals(fmt)) {
                        System.out.print("Download URL: ");
                        String url = scanner.nextLine().trim();
                        bookService.addBook(new EBook(bid, title, author, url));
                    } else {
                        System.out.print("Shelf Number: ");
                        int shelf = 101;
                        try { shelf = Integer.parseInt(scanner.nextLine().trim()); } catch (Exception ignored) {}
                        bookService.addBook(new PhysicalBook(bid, title, author, shelf));
                    }
                    System.out.println("✅ Book added successfully!");
                } else if ("6".equals(option) && "SELLER".equalsIgnoreCase(currentUser.getRole())) {
                    System.out.print("Book ID to delete: ");
                    String bid = scanner.nextLine().trim();
                    bookService.deleteBook(bid);
                    System.out.println("Book deleted.");
                } else if ("0".equals(option)) {
                    currentUser = null;
                }
            }
        }
    }
}
