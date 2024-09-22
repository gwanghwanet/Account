package net.gwanghwa.account.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;
import net.gwanghwa.account.domain.Account;
import net.gwanghwa.account.domain.AccountUser;
import net.gwanghwa.account.repository.AccountRepository;
import net.gwanghwa.account.repository.AccountUserRepository;
import net.gwanghwa.account.type.AccountStatus;

@Service
@RequiredArgsConstructor
public class AccountService {
	@Autowired
	private AccountUserRepository accountUserRepository;
	
	@Autowired
    private AccountRepository accountRepository;
    
	// 계좌 생성
    @Transactional
    public String createAccount(String requestBody) throws Exception {
    	JsonElement jsonElement = JsonParser.parseString(requestBody);
	    JsonObject jsonObject = jsonElement.getAsJsonObject();
	    
	    Long userId = Long.valueOf(jsonObject.get("userId").getAsLong());
	    Long initBalance = Long.valueOf(jsonObject.get("initBalance").getAsLong());
	    
	    AccountUser accountUser = accountUserRepository.findByAccountUser(userId).orElse(new AccountUser());
	    if(accountUser.getAccountUser() != userId) {
        	throw new Exception("USER_NOT_FOUND");
        }
    	
    	if(accountRepository.count() > 0 && accountRepository.countByAccountUser(accountUser) == 10) {
    		throw new Exception("MAX_ACCOUNT_COUNT");
    	}
    	
    	Long accountNumber;
		Random random = new Random(240921L);
    	do {
    		Long min = 1000000001L;
    		Long max = 1999999999L;
    		accountNumber = min + (long)(random.nextDouble() * (max - min + 1));
    	
    	} while (accountRepository.countByAccountNumber(accountNumber.toString()) !=  0);
    	 
        Account account = Account.builder()
        		.accountUser(accountUser)
                .accountNumber(accountNumber.toString())
                .accountStatus(AccountStatus.IN_USE)
                .balance(initBalance)
                .registeredAt(LocalDateTime.now())
                .unregistedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Account retAccount = accountRepository.save(account);
        
        JsonObject retJsonObject = new JsonObject();
        retJsonObject.addProperty("userId", retAccount.getAccountUser().getAccountUser().toString());
        retJsonObject.addProperty("accountNumber", retAccount.getAccountNumber());
        retJsonObject.addProperty("registeredAt", retAccount.getRegisteredAt().toString());
        
        Gson gson = new Gson();
        String retJsonString = gson.toJson(retJsonObject);
    	
    	return retJsonString;
    }
    
    // 계좌 해지
    @Transactional
    public String deleteAccount(String requestBody) throws Exception {
    	JsonElement jsonElement = JsonParser.parseString(requestBody);
	    JsonObject jsonObject = jsonElement.getAsJsonObject();
	    
	    Long userId = Long.valueOf(jsonObject.get("userId").getAsLong());
	    String accountNumber = jsonObject.get("accountNumber").getAsString();
	    
	    // 사용자 또는 계좌가 없는 경우
	    if( accountUserRepository.countByAccountUser(userId) == 0
	    		|| !accountRepository.findByAccountNumber(accountNumber).isPresent()) {
	    	throw new Exception("USER_OR_ACCOUNT_NUMBER_NOT_FOUND");
	    }
    	
	    Optional<Account> tmpAccount = accountRepository.findByAccountNumber(accountNumber);
	    Account account = tmpAccount.get();
	    
	    // 사용자 아이디와 계좌 소유주가 다른 경우
	    if( account.getAccountUser().getAccountUser() != userId) {
	    	throw new Exception("ACCOUNT_USER_MISMATCH");
	    }
	    
	    // 계좌가 이미 해지 상태인 경우
	    if( account.getAccountStatus() == AccountStatus.UNREGISTERED) {
	    	throw new Exception("ACCOUNT_ALREADY_UNREGISTERED");
	    }
	    
	    // 잔액이 있는 경우
	    if( account.getBalance() > 0) {
	    	throw new Exception("ACCOUNTBALANCE_EXISTS");
	    }
	    
	    // 계좌 정보 변경
	    account.setAccountStatus(AccountStatus.UNREGISTERED);
	    account.setUnregistedAt(LocalDateTime.now());
	    account.setUpdatedAt(LocalDateTime.now());
	    account = accountRepository.saveAndFlush(account);
	    
        
        JsonObject retJsonObject = new JsonObject();
        retJsonObject.addProperty("userId", account.getAccountUser().getAccountUser().toString());
        retJsonObject.addProperty("accountNumber", account.getAccountNumber());
        retJsonObject.addProperty("unregisteredAt", account.getUnregistedAt().toString());
        
        Gson gson = new Gson();
        String retJsonString = gson.toJson(retJsonObject);
    	
    	return retJsonString;
    }

    // 계좌 확인
    @Transactional
    public String getAccountInfo(Long userId) throws Exception {
    	AccountUser accountUser = accountUserRepository.findByAccountUser(userId).orElse(new AccountUser());
	    if(accountUser.getAccountUser() != userId) {
        	throw new Exception("USER_NOT_FOUND");
        }
    	
	    List<Account> listAccount = accountRepository.findByaccountUserAndAccountStatus(accountUser, AccountStatus.IN_USE);
	    if(listAccount.size() == 0) {
	    	throw new Exception("USE_ACCOUNT_NOT_FOUND");
	    }
	    
	    List<JsonObject> responeList = new ArrayList<JsonObject>();
	    for (Account account : listAccount) {
	    	JsonObject tempJsonObject = new JsonObject();
	    	tempJsonObject.addProperty("accountNumber", account.getAccountNumber());
	    	tempJsonObject.addProperty("balance", String.format("%dL", account.getBalance()));
	    	
	        responeList.add(tempJsonObject);
		}
	    
	    // Gson 인스턴스 생성
        Gson gson = new Gson();

        // List를 JSON으로 변환
        String respone = gson.toJson(responeList);
        
        return respone;
    }
}
