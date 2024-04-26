package com.dws.challenge.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.service.TransferService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/transfers")
@Slf4j
public class TransferController {

	@Autowired
	private TransferService transferService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> transferMoney(@RequestBody @Valid TransferRequest transferRequest) {
		log.info("Begin Transfer {}", transferRequest);
		try {
			transferService.transferMoney(transferRequest);
		} catch (IllegalArgumentException ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}
		log.info("End Transfer {}", transferRequest);
		return ResponseEntity.ok().build();
	}
}
