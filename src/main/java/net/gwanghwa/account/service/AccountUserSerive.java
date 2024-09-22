package net.gwanghwa.account.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.gwanghwa.account.domain.AccountUser;
import net.gwanghwa.account.repository.AccountUserRepository;

@Service
@RequiredArgsConstructor
public class AccountUserSerive implements CommandLineRunner{
	@Autowired
	private AccountUserRepository accountUserRepository;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		for(int i = 1 ; i <= 10; i++) {
			AccountUser accountUser = AccountUser.builder()
												 .accountUser(Long.valueOf(i))
												 .build();
			accountUserRepository.save(accountUser);
		}
		
		System.out.println("[AccountUserSerive::run] success");
	}
	
	@Transactional
	public AccountUser getAccountUser(Long id) {
		if(id < 0) {
			throw new RuntimeException("ID는 음수가 될 수 없습니다.");
		}
		return accountUserRepository.findById(id).get();
	}
}
