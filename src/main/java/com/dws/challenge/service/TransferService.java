package com.dws.challenge.service;

import java.util.Optional;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferRequest;

@Service
public class TransferService {

    private final AccountsService accountService;
    private final EmailNotificationService notificationService;

    @Autowired
    public TransferService(AccountsService accountService, EmailNotificationService notificationService) {
        this.accountService = accountService;
        this.notificationService = notificationService;
    }

    public void transferMoney(TransferRequest transferRequest) throws IllegalArgumentException {
    	Account accountFrom = accountService.getAccount(transferRequest.getAccountFromId());
    	Account accountTo = accountService.getAccount(transferRequest.getAccountToId());

        Lock lock1 = null;
        Lock lock2 = null;
        try {
            if (accountFrom != null && accountTo != null) {
                // Acquire locks for the involved accounts
                lock1 = accountFrom.getAccountId().compareTo(accountTo.getAccountId()) < 0 ? accountFrom.getLock() : accountTo.getLock();
                lock2 = accountFrom.getAccountId().compareTo(accountTo.getAccountId()) < 0 ? accountTo.getLock() : accountFrom.getLock();

                lock1.lock();
                lock2.lock();
                
                if (transferRequest.getAmount().signum() <= 0) {
                    throw new IllegalArgumentException("We do not support overdrafts!");
                }

                if (accountFrom.getBalance().compareTo(transferRequest.getAmount()) < 0) {
                    throw new IllegalArgumentException("Insufficient balance");
                }

                accountFrom.setBalance(accountFrom.getBalance().subtract(transferRequest.getAmount()));
                accountTo.setBalance(accountTo.getBalance().add(transferRequest.getAmount()));
            } else {
                throw new IllegalArgumentException("To or from account Id missing!");
            }
        } finally {
            // Release locks
        	if(lock1 != null)
        		lock1.unlock();
        	if(lock2 != null)
        		lock2.unlock();
        }

        // Send notifications
        notificationService.notifyAboutTransfer(accountTo, "Money Credited to " + accountTo.getAccountId());
        notificationService.notifyAboutTransfer(accountFrom, "Money debited from " + accountFrom.getAccountId());
    }
}
