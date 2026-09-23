package Model;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Book {
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private double buyPrice = 499.0;
    private boolean available = true;
    private int totalCopies = 5;
    private int availableCopies = 5;

    public Book(String bookId, String title, String author, String genre, double buyPrice) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre != null ? genre : "General";
        this.buyPrice = buyPrice > 0 ? buyPrice : 399.0;
        this.available = true;
        this.totalCopies = 5;
        this.availableCopies = 5;
    }

    public Book(String bookId, String title, String author, String genre, double buyPrice, boolean available) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre != null ? genre : "General";
        this.buyPrice = buyPrice > 0 ? buyPrice : 399.0;
        this.available = available;
        this.totalCopies = 5;
        this.availableCopies = available ? 5 : 0;
    }

    // Getters and Setters
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) {
        this.available = available;
        if (!available) {
            this.availableCopies = 0;
        } else if (this.availableCopies == 0) {
            this.availableCopies = 1;
        }
    }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
        this.available = availableCopies > 0;
    }

    // Polymorphism abstract methods
    public abstract double getRentalPrice(int days);
    public abstract String getBookType();

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("bookId", bookId);
        map.put("title", title);
        map.put("author", author);
        map.put("genre", genre);
        map.put("bookType", getBookType());
        map.put("available", available);
        map.put("dailyRate", getRentalPrice(1));
        map.put("buyPrice", buyPrice);
        map.put("availableCopies", availableCopies);
        map.put("totalCopies", totalCopies);
        return map;
    }
}