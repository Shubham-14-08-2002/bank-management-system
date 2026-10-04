package com.example.bms.controller;

import com.example.bms.DTO.TransferDto.CreateAccountRequest;
import com.example.bms.DTO.TransferDto.InternalTransferRequest;
import com.example.bms.model.Customer;
import com.example.bms.service.customerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final customerService customerService;

    public CustomerController(customerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> search(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(customerService.search(query));
    }

    @PostMapping
    public ResponseEntity<Customer> create(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(
            @PathVariable Long id, 
            @Valid @RequestBody com.example.bms.DTO.TransferDto.UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok("Account successfully removed.");
    }

    @PostMapping("/transfer/internal")
    public ResponseEntity<String> transferInternal(@Valid @RequestBody InternalTransferRequest request) {
        customerService.internalTransfer(request);
        return ResponseEntity.ok("Internal transfer completed successfully.");
    }
}