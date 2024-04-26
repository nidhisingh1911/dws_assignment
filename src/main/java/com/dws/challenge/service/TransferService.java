package com.dws.challenge.service;

import java.util.Optional;

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
		Account accountFrom = accountService.getAccount(Optional.of(transferRequest.getAccountFromId()).orElseThrow(() -> new IllegalArgumentException("Account not found")));
		Account accountTo = accountService.getAccount(Optional.of(transferRequest.getAccountToId()).orElseThrow(() -> new IllegalArgumentException("Account not found")));

		synchronized (this) {
			if(accountFrom != null && accountTo != null) {
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
		}

		// Send notifications
		notificationService.notifyAboutTransfer(accountTo, "Money Credited to " + accountTo.getAccountId());
		notificationService.notifyAboutTransfer(accountFrom, "Money debited from " + accountFrom.getAccountId());
	}
}