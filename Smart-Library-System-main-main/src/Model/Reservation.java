package Model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Reservation {
    private String reservationId;
    private String userId;
    private String bookId;
    private String bookTitle;
    private String reservationDate;
    private String status; // "PENDING", "READY", "FULFILLED", "CANCELLED"
    private int queuePosition = 1;

    public Reservation(String reservationId, String userId, String bookId) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.bookId = bookId;
        this.status = "PENDING";
        this.queuePosition = 1;
    }

    public Reservation(String reservationId, String userId, String bookId, String bookTitle,
                       String reservationDate, String status) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reservationDate = reservationDate;
        this.status = status;
        this.queuePosition = 1;
    }

    public Reservation(String reservationId, String userId, String bookId, String bookTitle,
                       String reservationDate, String status, int queuePosition) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reservationDate = reservationDate;
        this.status = status;
        this.queuePosition = queuePosition;
    }

    // Getters and Setters
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getReservationDate() { return reservationDate; }
    public void setReservationDate(String reservationDate) { this.reservationDate = reservationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("reservationId", reservationId);
        map.put("userId", userId);
        map.put("bookId", bookId);
        map.put("bookTitle", bookTitle != null ? bookTitle : "");
        map.put("reservationDate", reservationDate != null ? reservationDate : "");
        map.put("status", status);
        map.put("queuePosition", queuePosition);
        return map;
    }
}
