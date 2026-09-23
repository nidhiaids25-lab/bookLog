package Model;

import java.util.Map;

public class EBook extends Book implements DigitalAccessible {
    private String downloadLink;

    public EBook(String bookId, String title, String author, String downloadLink) {
        super(bookId, title, author, "Computer Science", 199.0);
        this.downloadLink = downloadLink;
    }

    public EBook(String bookId, String title, String author, String downloadLink, boolean available) {
        super(bookId, title, author, "Computer Science", 199.0, available);
        this.downloadLink = downloadLink;
    }

    public EBook(String bookId, String title, String author, String genre, String downloadLink, double buyPrice, boolean available) {
        super(bookId, title, author, genre, buyPrice, available);
        this.downloadLink = downloadLink;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    // Polymorphism: EBook digital rate is ₹10/day
    @Override
    public double getRentalPrice(int days) {
        return days * 10.0;
    }

    @Override
    public String getBookType() {
        return "EBOOK";
    }

    @Override
    public void download() {
        System.out.println("Downloading eBook from: " + downloadLink);
    }

    @Override
    public String getAccessUrl() {
        return downloadLink;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("downloadLink", downloadLink);
        return map;
    }
}