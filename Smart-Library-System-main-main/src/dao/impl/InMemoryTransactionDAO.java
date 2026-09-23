package dao.impl;

import Model.Transaction;
import dao.TransactionDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-Memory thread-safe implementation of TransactionDAO.
 */
public class InMemoryTransactionDAO implements TransactionDAO {
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();

    public InMemoryTransactionDAO() {
        seedSampleTransactions();
    }

    private void seedSampleTransactions() {
        // Pre-seeded active loan for demo: Nidhi has borrowed B103
        Transaction t1 = new Transaction(
            "TX-1001",
            "U101",
            "B103",
            "Design Patterns: Elements of Reusable Object-Oriented Software",
            "2026-09-15",
            "2026-09-29",
            null,
            280.0,
            "BORROWED"
        );
        addTransaction(t1);

        // Pre-seeded returned transaction
        Transaction t2 = new Transaction(
            "TX-1002",
            "U103",
            "B101",
            "Clean Code: A Handbook of Agile Software Craftsmanship",
            "2026-09-01",
            "2026-09-15",
            "2026-09-14",
            260.0,
            "RETURNED"
        );
        addTransaction(t2);
    }

    @Override
    public void addTransaction(Transaction tx) {
        if (tx != null && tx.getTransactionId() != null) {
            transactions.put(tx.getTransactionId(), tx);
        }
    }

    @Override
    public Transaction getTransactionById(String transactionId) {
        if (transactionId == null) return null;
        return transactions.get(transactionId);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    @Override
    public List<Transaction> getTransactionsByUserId(String userId) {
        if (userId == null) return Collections.emptyList();
        return transactions.values().stream()
                .filter(t -> userId.equalsIgnoreCase(t.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateTransaction(Transaction tx) {
        if (tx != null && tx.getTransactionId() != null && transactions.containsKey(tx.getTransactionId())) {
            transactions.put(tx.getTransactionId(), tx);
            return true;
        }
        return false;
    }
}
