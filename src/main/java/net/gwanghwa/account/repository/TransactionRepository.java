package net.gwanghwa.account.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.gwanghwa.account.domain.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	Optional<Transaction> findByTransactionId(String transactionId);
	
	
}
