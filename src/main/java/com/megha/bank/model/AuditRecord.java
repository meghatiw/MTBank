package com.megha.bank.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "mt_audit")
@Data
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tradeRef;
    @Column(length = 4000)
    private String incomingMessage;
    @Column(length = 4000)
    private String outgoingMessage;
    private String status; // ACK/NAK
    private LocalDateTime createdAt;
}
