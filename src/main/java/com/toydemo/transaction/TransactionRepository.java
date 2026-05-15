package com.toydemo.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByUniqueIdentifier(String uniqueIdentifier);
}
