package com.example.bms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bms.model.TransferRecord;

@Repository
public interface TransferRepository extends JpaRepository<TransferRecord, Long> {
}