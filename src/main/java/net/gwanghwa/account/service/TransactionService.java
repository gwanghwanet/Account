package net.gwanghwa.account.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;
import net.gwanghwa.account.domain.Account;
import net.gwanghwa.account.domain.AccountUser;
import net.gwanghwa.account.domain.Transaction;
import net.gwanghwa.account.repository.AccountRepository;
import net.gwanghwa.account.repository.AccountUserRepository;
import net.gwanghwa.account.repository.TransactionRepository;
import net.gwanghwa.account.type.AccountStatus;
import net.gwanghwa.account.type.TransactionResultType;
import net.gwanghwa.account.type.TransactionType;

@Service
@RequiredArgsConstructor
public class TransactionService {
	@Autowired
	private AccountUserRepository accountUserRepository;
	
	@Autowired
    private AccountRepository accountRepository;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private final RedissonClient redissonClient;

	// 잔액 사용
    @Transactional
    public String useBalance(String requestBody) throws Exception {
    	JsonElement jsonElement = JsonParser.parseString(requestBody);
	    JsonObject jsonObject = jsonElement.getAsJsonObject();
	    
	    Long userId = Long.valueOf(jsonObject.get("userId").toString());
	    String accountNumber = jsonObject.get("accountNumber").toString();
	    Long amount = Long.valueOf(jsonObject.get("amount").toString());
	    
	    AccountUser accountUser = accountUserRepository.findByAccountUser(userId).orElse(new AccountUser());
	    // 사용자가 없는 경우
	    if(accountUser.getAccountUser() != userId) {
        	throw new Exception("USER_NOT_FOUND");
        }
    	
	    Optional<Account> tmpAccount = accountRepository.findByAccountNumber(accountNumber);
	    Account account = tmpAccount.get();

	    // 사용자 아이디와 계좌 소유주가 다른 경우
	    if(account.getAccountUser().getAccountUser() != userId) {
	    	throw new Exception("ACCOUNT_USER_MISMATCH");
	    }
	    
	    // 계좌가 이미 해지 상태인 경우
	    if(account.getAccountStatus() == AccountStatus.UNREGISTERED) {
	    	throw new Exception("ACCOUNT_ALREADY_UNREGISTERED");
	    }
	    
	    // 거래금액이 잔액보다 큰 경우
	    if(amount > account.getBalance()) {
	    	throw new Exception("AMOUNT_IS_GREATER_THAN_ACCOUNT_BALANCE");
	    }
    	
	    // 거래금액이 너무 작거나 큰 경우
	    if(amount < 0 || amount > 1000000 ) {
	    	throw new Exception("AMOUNT_CHECK_REQUIRED");
	    }
    	
	    
	    RLock rlock = getLockForAccountNumber(accountNumber);
	    String transactionId = new String(UUID.randomUUID().toString().replaceAll("\\-", ""));
	    if(rlock != null) {
	    	Transaction transaction = Transaction.builder()
	    			.transactionType(TransactionType.USE)
		    		.transactionResultType(TransactionResultType.S)
		    		.account(account)
		    		.amount(amount)
		    		.balanceSnapshot(account.getBalance() - amount)
		    		.transactionId(transactionId)
		    		.transactedAt(LocalDateTime.now())
		    		.createdAt(LocalDateTime.now())
		    		.updatedAt(LocalDateTime.now())
		    		.build();
		    transaction = transactionRepository.save(transaction); 
		    
		    jsonObject = new JsonObject();
		    jsonObject.addProperty("accountNumber", transaction.getAccount().getAccountNumber());
		    jsonObject.addProperty("transactionResult", transaction.getTransactionResultType().toString());
		    jsonObject.addProperty("transactionId", transaction.getTransactionId());
		    jsonObject.addProperty("amount", transaction.getAmount());
		    jsonObject.addProperty("transactedAt", transaction.getTransactedAt().toString());
	    } else {
	    	jsonObject = new JsonObject();
		    jsonObject.addProperty("accountNumber", accountNumber);
		    jsonObject.addProperty("transactionResult", TransactionResultType.F.toString());
		    jsonObject.addProperty("transactionId", transactionId);
		    jsonObject.addProperty("amount", amount);
		    jsonObject.addProperty("transactedAt", LocalDateTime.now().toString());
	    }
	    
        Gson gson = new Gson();
        String strGson = gson.toJson(jsonObject);
    	
    	return strGson;
    }

	// 잔액 사용 취소
    @Transactional
    public String cancelBalance(String requestBody) throws Exception {
    	JsonElement jsonElement = JsonParser.parseString(requestBody);
	    JsonObject jsonObject = jsonElement.getAsJsonObject();
	    
	    String transactionId = jsonObject.get("transactionId").toString();
	    String accountNumber = jsonObject.get("accountNumber").toString();
	    Long amount = Long.valueOf(jsonObject.get("amount").toString());
	    
	    Optional<Transaction> optTransaction = transactionRepository.findByTransactionId(transactionId);
	    Transaction transaction = optTransaction.get();
	    
	    // 거래 아이디에 해당하는 거래가 없는 경우
	    if(transactionId.compareTo(transaction.getTransactionId()) != 0) {
        	throw new Exception("TRANSACTION_NOT_FOUND");
        }
	    
	    Optional<Account> optAccount = accountRepository.findByAccountNumber(accountNumber);
	    Account account = optAccount.get();
	    
	    // 계좌가 없는 경우
	    if(account == null) {
	    	throw new Exception("ACCOUNT_NOT_FOUND");
	    }
	    
	    // 거래와 계좌가 일치하지 않는 경우
	    if(!transaction.getAccount().equals(account)) {
	    	throw new Exception("TRANSACTION_AND_ACCOUNT_MISMATCH");
	    }
    	
	    // 거래금액과 거래 취소 금액이 다른경우(부분 취소 불가능)
	    if(amount.compareTo(transaction.getAmount()) == 0 ) {
	    	throw new Exception("PARTIAL_CANCELLATION_IMPOSSIBILITY");
	    }

	    transactionId = new String(UUID.randomUUID().toString().replaceAll("\\-", ""));
	    if(transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1L))) {
	    	jsonObject = new JsonObject();
		    jsonObject.addProperty("accountNumber", accountNumber);
		    jsonObject.addProperty("transactionResult", TransactionResultType.F.toString());
		    jsonObject.addProperty("transactionId", transactionId);
		    jsonObject.addProperty("amount", amount);
		    jsonObject.addProperty("transactedAt", LocalDateTime.now().toString());
	    } else {
		    RLock rlock = getLockForAccountNumber(accountNumber);
		    if(rlock != null) {
		    	Transaction tmpTran = Transaction.builder()
		    			.transactionType(TransactionType.USE_CANCLE)
			    		.transactionResultType(TransactionResultType.S)
			    		.account(account)
			    		.amount(amount)
			    		.balanceSnapshot(account.getBalance() + amount)
			    		.transactionId(transactionId)
			    		.transactedAt(LocalDateTime.now())
			    		.createdAt(LocalDateTime.now())
			    		.updatedAt(LocalDateTime.now())
			    		.build();
			    transaction = transactionRepository.save(tmpTran); 
			    
			    jsonObject = new JsonObject();
			    jsonObject.addProperty("accountNumber", transaction.getAccount().getAccountNumber());
			    jsonObject.addProperty("transactionResult", transaction.getTransactionResultType().toString());
			    jsonObject.addProperty("transactionId", transaction.getTransactionId());
			    jsonObject.addProperty("amount", transaction.getAmount());
			    jsonObject.addProperty("transactedAt", transaction.getTransactedAt().toString());
			    
			    rlock.unlock();
		    } else {
		    	jsonObject = new JsonObject();
			    jsonObject.addProperty("accountNumber", accountNumber);
			    jsonObject.addProperty("transactionResult", TransactionResultType.F.toString());
			    jsonObject.addProperty("transactionId", transactionId);
			    jsonObject.addProperty("amount", amount);
			    jsonObject.addProperty("transactedAt", LocalDateTime.now().toString());
		    }
	    }
	    
        Gson gson = new Gson();
        String strGson = gson.toJson(jsonObject);
    	
    	return strGson;
    }
    
    // 거래 확인
    @Transactional
    public String getTransactionInfo(String transactionId) throws Exception {
    	Optional<Transaction> optTransaction = transactionRepository.findByTransactionId(transactionId);
	    Transaction transaction = optTransaction.get();
	    
	    // 거래 아이디에 해당하는 거래가 없는 경우
	    if(transactionId.compareTo(transaction.getTransactionId()) != 0) {
        	throw new Exception("TRANSACTION_NOT_FOUND");
        }
	    
	    JsonObject jsonObject = new JsonObject();
	    jsonObject.addProperty("accountNumber", transaction.getAccount().getAccountNumber());
	    jsonObject.addProperty("transactionResult", transaction.getTransactionResultType().toString());
	    jsonObject.addProperty("transactionId", transaction.getTransactionId());
	    jsonObject.addProperty("amount", transaction.getAmount());
	    jsonObject.addProperty("transactedAt", transaction.getTransactedAt().toString());
	    
	    // Gson 인스턴스 생성
        Gson gson = new Gson();

        // List를 JSON으로 변환
        String respone = gson.toJson(jsonObject);
        
        return respone;
    }
    private RLock getLockForAccountNumber(String accountNumber) {
        RLock lock = redissonClient.getLock("lock:" + accountNumber);

        boolean isLock = false;
        try {
            isLock = lock.tryLock(1, -1, TimeUnit.SECONDS);
            if(!isLock) {
            	lock = null;
                System.err.println("======Lock acquisition failed=====");
                return lock;
            }
        } catch (Exception e) {
        	System.err.println("Redis lock failed");
        }

        return lock;
    }
    
}
