package net.gwanghwa.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.gwanghwa.account.domain.Account;
import net.gwanghwa.account.domain.AccountUser;
import net.gwanghwa.account.type.AccountStatus;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	
	Long countByAccountUser(AccountUser accountUser);
	
	Long countByAccountNumber(String accountNumber);
	
	Optional<Account> findByAccountNumber(String accountNumber);
	
	List<Account> findByaccountUserAndAccountStatus(AccountUser accountUser, AccountStatus accountStatus);
}
