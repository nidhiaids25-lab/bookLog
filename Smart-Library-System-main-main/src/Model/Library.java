package Model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Library {
    private String libraryId;
    private String name;
    private String city;
    private String pincode;
    private String location;
    private double distanceKm = 1.0;
    private String contactNumber;
    private List<Book> books = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    public Library() {
        this.libraryId = "LIB-001";
        this.name = "Central Metro Library";
        this.city = "Delhi";
        this.pincode = "110001";
        this.location = "Connaught Place, Central Wing";
        this.distanceKm = 1.2;
        this.contactNumber = "+91 11 2345 6789";
    }

    public Library(String libraryId, String name, String city, String pincode, String location, double distanceKm, String contactNumber) {
        this.libraryId = libraryId;
        this.name = name;
        this.city = city;
        this.pincode = pincode;
        this.location = location;
        this.distanceKm = distanceKm;
        this.contactNumber = contactNumber;
    }

    public boolean matchesLocation(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.trim().toLowerCase();
        return (city != null && city.toLowerCase().contains(q)) ||
               (pincode != null && pincode.contains(q)) ||
               (name != null && name.toLowerCase().contains(q)) ||
               (location != null && location.toLowerCase().contains(q));
    }

    // Getters and Setters
    public String getLibraryId() { return libraryId; }
    public void setLibraryId(String libraryId) { this.libraryId = libraryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public void addBook(Book book) { books.add(book); }
    public void addUser(User user) { users.add(user); }
    public List<Book> getBooks() { return books; }
    public List<User> getUsers() { return users; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("libraryId", libraryId);
        map.put("name", name);
        map.put("city", city);
        map.put("pincode", pincode);
        map.put("location", location);
        map.put("distanceKm", distanceKm);
        map.put("contactNumber", contactNumber != null ? contactNumber : "");
        map.put("totalBooks", books.size());
        return map;
    }
}