package Model;

import java.util.Map;

public class PhysicalBook extends Book implements Borrowable {
    private int shelfNumber;

    public PhysicalBook(String bookId, String title, String author, int shelfNumber) {
        super(bookId, title, author, "Computer Science", 499.0);
        this.shelfNumber = shelfNumber;
    }

    public PhysicalBook(String bookId, String title, String author, int shelfNumber, boolean available) {
        super(bookId, title, author, "Computer Science", 499.0, available);
        this.shelfNumber = shelfNumber;
    }

    public PhysicalBook(String bookId, String title, String author, String genre, int shelfNumber, double buyPrice, boolean available) {
        super(bookId, title, author, genre, buyPrice, available);
        this.shelfNumber = shelfNumber;
    }

    public int getShelfNumber() {
        return shelfNumber;
    }

    public void setShelfNumber(int shelfNumber) {
        this.shelfNumber = shelfNumber;
    }

    // Polymorphism: Physical Book rental is ₹20/day
    @Override
    public double getRentalPrice(int days) {
        return days * 20.0;
    }

    @Override
    public String getBookType() {
        return "PHYSICAL";
    }

    @Override
    public void borrowBook() {
        int avail = getAvailableCopies();
        if (avail > 0) {
            setAvailableCopies(avail - 1);
        } else {
            setAvailable(false);
        }
        System.out.println("Physical book issued successfully from shelf: " + shelfNumber);
    }

    @Override
    public void returnBook() {
        setAvailableCopies(getAvailableCopies() + 1);
        setAvailable(true);
        System.out.println("Physical book returned to shelf: " + shelfNumber);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("shelfNumber", shelfNumber);
        return map;
    }
}