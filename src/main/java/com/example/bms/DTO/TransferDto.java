package com.example.bms.DTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransferDto {

    public record CreateAccountRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit number")
        String phoneNumber,

        @NotBlank(message = "Identification type is required")
        @Pattern(regexp = "^(PAN|AADHAAR)$", message = "ID Type must be either PAN or AADHAAR")
        String idType,

        @NotBlank(message = "ID number is required")
        String idNumber,

        @NotNull(message = "Opening balance is required")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal accountBalance
    ) {}

    public record InternalTransferRequest(
        @NotBlank(message = "Source account is required") 
        String fromAccount,

        @NotBlank(message = "Destination account is required") 
        String toAccount,

        @NotNull(message = "Amount is required") 
        @DecimalMin(value = "1.00", message = "Minimum transfer amount is 1.00") 
        BigDecimal amount
    ) {}

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}

    public record UpdateCustomerRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit number")
        String phoneNumber
    ) {}
}