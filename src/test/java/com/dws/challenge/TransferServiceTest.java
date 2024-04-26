package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import com.dws.challenge.service.TransferService;

@SpringBootTest
public class TransferServiceTest {

	@MockBean
	private AccountsService accountService;

	@MockBean
	private EmailNotificationService notificationService;

	@Autowired
	private TransferService transferService;

	@Test
	public void testTransferMoney_SuccessfulTransfer() {
		// Mock accounts
		Account accountFrom = new Account("Id-123", BigDecimal.valueOf(100.0));
		Account accountTo = new Account("Id-234", BigDecimal.valueOf(50.0));

		TransferRequest transferRequest = new TransferRequest();
		transferRequest.setAccountFromId("Id-123");
		transferRequest.setAccountToId("Id-234");
		transferRequest.setAmount(BigDecimal.TEN); // For example, transferring $10

		// Mock behavior of accountService
		when(accountService.getAccount("Id-123")).thenReturn(accountFrom);
		when(accountService.getAccount("Id-234")).thenReturn(accountTo);

		// Perform transfer
		transferService.transferMoney(transferRequest);

		// Verify balances and notifications
		assertThat(BigDecimal.valueOf(90.0)).isEqualTo(accountFrom.getBalance());
		assertThat(BigDecimal.valueOf(60.0)).isEqualTo(accountTo.getBalance());

		verify(notificationService, times(1)).notifyAboutTransfer(eq(accountFrom), anyString());
		verify(notificationService, times(1)).notifyAboutTransfer(eq(accountTo), anyString());
	}

	// Test cases to cover edge cases and failure scenarios
	@Test
	public void testTransferMoney_UnsuccessfulTransfer() {
		// Mock accounts
		Account accountFrom = new Account("Id-123", BigDecimal.valueOf(100.0));
		Account accountTo = new Account("Id-234", BigDecimal.valueOf(50.0));

		TransferRequest transferRequest = new TransferRequest();
		transferRequest.setAccountFromId("Id-123");
		transferRequest.setAccountToId("Id-234");
		transferRequest.setAmount(BigDecimal.valueOf(1000)); //Transfer amount greater than balance

		// Mock behavior of accountService
		when(accountService.getAccount("Id-123")).thenReturn(accountFrom);
		when(accountService.getAccount("Id-234")).thenReturn(accountTo);

		try {
			// Perform transfer
			transferService.transferMoney(transferRequest);
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage()).isEqualTo("Insufficient balance");
		}
	}

	@Test
	public void testTransferMoney_NegativeTransfer() {
		// Mock accounts
		Account accountFrom = new Account("Id-123", BigDecimal.valueOf(100.0));
		Account accountTo = new Account("Id-234", BigDecimal.valueOf(50.0));

		TransferRequest transferRequest = new TransferRequest();
		transferRequest.setAccountFromId("Id-123");
		transferRequest.setAccountToId("Id-234");
		transferRequest.setAmount(BigDecimal.valueOf(-1000)); //Transfer Negative amount

		// Mock behavior of accountService
		when(accountService.getAccount("Id-123")).thenReturn(accountFrom);
		when(accountService.getAccount("Id-234")).thenReturn(accountTo);

		try {
			// Perform transfer
			transferService.transferMoney(transferRequest);
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage()).isEqualTo("We do not support overdrafts!");
		}
	}
}

