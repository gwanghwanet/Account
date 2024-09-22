package net.gwanghwa.account.controller;

import lombok.RequiredArgsConstructor;
import net.gwanghwa.account.service.AccountService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/account")
    public String createAccount(@RequestBody String requestBody) throws Exception {
    	return accountService.createAccount(requestBody);
    }

    @DeleteMapping("/account")
    public String deleteAccount(@RequestBody String requestBody) throws Exception {
    	return accountService.deleteAccount(requestBody);
    }
    
    @GetMapping("/account")
    public String getAccountInfo(@RequestParam("user_id") Long user_id) throws Exception {
    	return accountService.getAccountInfo(user_id);
    }
}
