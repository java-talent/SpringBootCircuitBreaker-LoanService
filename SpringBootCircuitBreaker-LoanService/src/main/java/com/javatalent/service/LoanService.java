package com.javatalent.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.javatalent.dto.InterestRate;
import com.javatalent.entity.Loan;
import com.javatalent.repo.LoanRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class LoanService {
	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private RestTemplate restTemplate;

	private static final String SERVICE_NAME = "loan-service";
	private static final String RATE_SERVICE_URL = "http://localhost:9000/api/rates/";

	@CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "getDefaultLoans")
	public List<Loan> getAllLoansByType(String type) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<InterestRate> entity = new HttpEntity<>(null, headers);

		ResponseEntity<InterestRate> response = restTemplate.exchange(RATE_SERVICE_URL + type, HttpMethod.GET, entity,
				InterestRate.class);
		InterestRate rate = response.getBody();
		List<Loan> loanList = new ArrayList<>();
		if (rate != null) {
			loanList = loanRepository.findByType(type);
			for (Loan loan : loanList) {
				loan.setInterest(loan.getAmount() * (rate.getRateValue() / 100));
			}
		}
		return loanList;
	}
	
	public List<Loan> getDefaultLoans(Exception e){
		System.out.println("fallBack Method called!");
		return new ArrayList<Loan>();
	}
}