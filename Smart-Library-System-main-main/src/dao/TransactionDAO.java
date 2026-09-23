package dao;

import Model.Transaction;
import java.util.List;

/**
 * Interface defining Transaction Data Access operations for borrowing and returning books.
 */
public interface TransactionDAO {
    void addTransaction(Transaction tx);
    Transaction getTransactionById(String transactionId);
    List<Transaction> getAllTransactions();
    List<Transaction> getTransactionsByUserId(String userId);
    boolean updateTransaction(Transaction tx);
}
