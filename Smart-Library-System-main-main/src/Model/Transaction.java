package Model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Transaction {
    private String transactionId;
    private String userId;
    private String bookId;
    private String bookTitle;
    private TransactionType transactionType = TransactionType.RENT; // RENT or BUY
    private String issueDate; // YYYY-MM-DD
    private String dueDate;   // YYYY-MM-DD
    private String returnDate; // YYYY-MM-DD (null if not returned or if BUY)
    private double amountPaid;
    private double overdueFine = 0.0;
    private String status;    // "BORROWED", "RETURNED", "PURCHASED"

    public Transaction(String transactionId, String userId, String bookId) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.bookId = bookId;
        this.transactionType = TransactionType.RENT;
        this.status = "BORROWED";
    }

    public Transaction(String transactionId, String userId, String bookId, String bookTitle,
                       TransactionType type, String issueDate, String dueDate, String returnDate,
                       double amountPaid, double overdueFine, String status) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.transactionType = type != null ? type : TransactionType.RENT;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.amountPaid = amountPaid;
        this.overdueFine = overdueFine;
        this.status = status;
    }

    public Transaction(String transactionId, String userId, String bookId, String bookTitle,
                       String issueDate, String dueDate, String returnDate,
                       double amountPaid, double overdueFine, String status) {
        this(transactionId, userId, bookId, bookTitle, TransactionType.RENT, issueDate, dueDate, returnDate, amountPaid, overdueFine, status);
    }

    public Transaction(String transactionId, String userId, String bookId, String bookTitle,
                       String issueDate, String dueDate, String returnDate,
                       double amountPaid, String status) {
        this(transactionId, userId, bookId, bookTitle, TransactionType.RENT, issueDate, dueDate, returnDate, amountPaid, 0.0, status);
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public double getRentalPrice() { return amountPaid; }
    public void setRentalPrice(double rentalPrice) { this.amountPaid = rentalPrice; }

    public double getOverdueFine() { return overdueFine; }
    public void setOverdueFine(double overdueFine) { this.overdueFine = overdueFine; }

    public double getTotalAmount() { return amountPaid + overdueFine; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("transactionId", transactionId);
        map.put("userId", userId);
        map.put("bookId", bookId);
        map.put("bookTitle", bookTitle != null ? bookTitle : "");
        map.put("transactionType", transactionType.name());
        map.put("issueDate", issueDate != null ? issueDate : "");
        map.put("dueDate", dueDate != null ? dueDate : "N/A");
        map.put("returnDate", returnDate != null ? returnDate : "");
        map.put("amountPaid", amountPaid);
        map.put("rentalPrice", amountPaid);
        map.put("overdueFine", overdueFine);
        map.put("totalAmount", getTotalAmount());
        map.put("status", status);
        return map;
    }
}