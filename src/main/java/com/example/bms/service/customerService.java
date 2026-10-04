package com.example.bms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.bms.DTO.TransferDto.CreateAccountRequest;
import com.example.bms.DTO.TransferDto.InternalTransferRequest;
import com.example.bms.model.Customer;
import com.example.bms.model.TransferRecord;
import com.example.bms.repository.customerRepository;
import com.example.bms.repository.TransferRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class customerService {

    private final customerRepository customerRepository;
    private final TransferRepository transferRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
    private static final Pattern AADHAAR_PATTERN = Pattern.compile("^(\\d{12}|\\d{4}-\\d{4}-\\d{4})$");

    public customerService(customerRepository customerRepository, TransferRepository transferRepository) {
        this.customerRepository = customerRepository;
        this.transferRepository = transferRepository;
    }

    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer record not found for ID: " + id));
    }

    public List<Customer> search(String query) {
        if (query == null || query.isBlank()) {
            return customerRepository.findAll();
        }
        return customerRepository.searchCustomers(query.trim());
    }

    @Transactional
    public Customer createCustomer(CreateAccountRequest req) {
        if (customerRepository.findByEmailIgnoreCase(req.email().trim()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists: " + req.email());
        }

        validateIdentification(req.idType(), req.idNumber());

        String generatedAccountNumber = generateUniqueAccountNumber();

        Customer customer = new Customer(
            generatedAccountNumber,
            req.firstName().trim(),
            req.lastName().trim(),
            req.email().trim().toLowerCase(),
            req.phoneNumber().trim(),
            req.idType().toUpperCase(),
            req.idNumber().trim().toUpperCase(),
            req.accountBalance()
        );

        return customerRepository.save(customer);
    }

    private void validateIdentification(String type, String number) {
        String cleanNumber = number.trim().toUpperCase();
        if ("PAN".equalsIgnoreCase(type)) {
            if (!PAN_PATTERN.matcher(cleanNumber).matches()) {
                throw new IllegalArgumentException("Invalid PAN format! Must follow ABCDE1234F.");
            }
        } else if ("AADHAAR".equalsIgnoreCase(type)) {
            if (!AADHAAR_PATTERN.matcher(cleanNumber).matches()) {
                throw new IllegalArgumentException("Invalid Aadhaar format! Must be 12 digits (e.g. 123456789012 or 1234-5678-9012).");
            }
        } else {
            throw new IllegalArgumentException("Unsupported identification type: " + type);
        }
    }

    private String generateUniqueAccountNumber() {
        String accountNo;
        do {
            int randomNum = 100000 + secureRandom.nextInt(900000);
            accountNo = "ACC" + randomNum;
        } while (customerRepository.findByAccountNumber(accountNo).isPresent());
        return accountNo;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void internalTransfer(InternalTransferRequest req) {
        if (req.fromAccount().equalsIgnoreCase(req.toAccount())) {
            throw new IllegalArgumentException("Sender and receiver accounts cannot be identical.");
        }

        Customer sender = customerRepository.findByAccountNumber(req.fromAccount())
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found: " + req.fromAccount()));

        Customer receiver = customerRepository.findByAccountNumber(req.toAccount())
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found: " + req.toAccount()));

        if (sender.getAccountBalance().compareTo(req.amount()) < 0) {
            throw new IllegalStateException("Insufficient funds in account: " + req.fromAccount());
        }

        sender.setAccountBalance(sender.getAccountBalance().subtract(req.amount()));
        customerRepository.save(sender);

        receiver.setAccountBalance(receiver.getAccountBalance().add(req.amount()));
        customerRepository.save(receiver);

        transferRepository.save(new TransferRecord(
                sender.getAccountNumber(), 
                receiver.getAccountNumber(), 
                req.amount(), 
                LocalDateTime.now()
        ));
    }

    @Transactional
    public Customer updateCustomer(Long id, com.example.bms.DTO.TransferDto.UpdateCustomerRequest req) {
        return customerRepository.findById(id).map(existing -> {
            customerRepository.findByEmailIgnoreCase(req.email().trim().toLowerCase())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Email is already registered to another account: " + req.email());
                });

            existing.setFirstName(req.firstName().trim());
            existing.setLastName(req.lastName().trim());
            existing.setEmail(req.email().trim().toLowerCase());
            existing.setPhoneNumber(req.phoneNumber().trim());
            return customerRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Customer record not found for ID: " + id));
}

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        if (customer.getAccountBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot delete an account with an active balance (> 0.00). Withdraw funds first.");
        }
        customerRepository.delete(customer);
    }
}