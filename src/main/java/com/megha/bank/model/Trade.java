package com.megha.bank.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Data
public class Trade {

    public enum Type { BUY, SELL, CANCEL }
    public enum Status { NEW, ACKED, REJECTED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String tradeRef;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String instrument;
    private Long quantity;
    private Double price;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
