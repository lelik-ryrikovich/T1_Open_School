package ru.t1.account_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.account_processing.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
