package com.megha.bank.repository;

import com.megha.bank.model.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditRecord, Long> {
}
