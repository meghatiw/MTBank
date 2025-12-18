package com.megha.bank.service;

import com.megha.bank.model.AuditRecord;
import com.megha.bank.repository.AuditRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public AuditRecord record(String tradeRef, String incoming, String outgoing, String status) {
        AuditRecord r = new AuditRecord();
        r.setTradeRef(tradeRef);
        r.setIncomingMessage(incoming);
        r.setOutgoingMessage(outgoing);
        r.setStatus(status);
        r.setCreatedAt(LocalDateTime.now());
        return auditRepository.save(r);
    }
}
