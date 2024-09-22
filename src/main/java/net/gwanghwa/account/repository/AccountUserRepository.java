package net.gwanghwa.account.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.gwanghwa.account.domain.AccountUser;

@Repository
public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {
	
	Optional<AccountUser> findByAccountUser(Long accountUser);

	Long countByAccountUser(Long accountUser);
}
