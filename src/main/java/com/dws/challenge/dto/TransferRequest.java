package com.dws.challenge.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransferRequest {
    private String accountFromId;
    private String accountToId;
    private BigDecimal amount;
}