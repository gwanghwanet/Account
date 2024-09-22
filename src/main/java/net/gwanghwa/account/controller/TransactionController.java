package net.gwanghwa.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.gwanghwa.account.service.TransactionService;

@RestController
@RequiredArgsConstructor
public class TransactionController {
	private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    public String useBalance(@RequestBody String requestBody) throws Exception {
    	return transactionService.useBalance(requestBody);
    }
    
    @PostMapping("/transaction/cancel")
    public String cancelBalance(@RequestBody String requestBody) throws Exception {
    	return transactionService.cancelBalance(requestBody);
    }
    
    @GetMapping("/transaction")
    public String getTransactionInfo(@RequestParam("transaction_id") String transactionId) throws Exception {
    	return transactionService.getTransactionInfo(transactionId);
    }
}
