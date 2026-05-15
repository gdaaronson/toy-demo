package com.toydemo.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByUniqueIdentifier(String uniqueIdentifier);

    Optional<Transaction> findByUniqueIdentifier(String uniqueIdentifier);
}
