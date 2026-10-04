package com.example.bms.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.bms.DTO.TransferDto.LoginRequest;
import com.example.bms.security.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        if ("admin".equals(req.username()) && "admin123".equals(req.password())) {
            String token = jwtUtil.generateToken(req.username(), "ROLE_MANAGER");
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
    }
}