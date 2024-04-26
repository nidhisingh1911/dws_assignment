package com.dws.challenge;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.TransferService;
import com.dws.challenge.web.TransferController;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TransferController.class)
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;
    
    @MockBean
    private AccountsService accountService;

    @Test
    public void testTransferMoney_SuccessfulTransfer() throws Exception {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setAccountFromId("fromAccountId");
        transferRequest.setAccountToId("toAccountId");
        transferRequest.setAmount(BigDecimal.TEN);

        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transferRequest)))
                .andExpect(status().isOk());

        verify(transferService).transferMoney(transferRequest);
    }

    @Test
    public void testTransferMoney_InsufficientBalance() throws Exception {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setAccountFromId("fromAccountId");
        transferRequest.setAccountToId("toAccountId");
        transferRequest.setAmount(BigDecimal.valueOf(1000)); // Assuming insufficient balance

        doThrow(new IllegalArgumentException("Insufficient balance")).when(transferService).transferMoney(transferRequest);

        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient balance"));

        verify(transferService).transferMoney(transferRequest);
    }
    
    @Test
    void transferNoBody() throws Exception {
      this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    // Utility method to convert object to JSON string
    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

