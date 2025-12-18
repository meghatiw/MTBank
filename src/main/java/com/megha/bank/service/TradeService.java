package com.megha.bank.service;

import com.megha.bank.model.Trade;
import com.megha.bank.model.AuditRecord;
import com.megha.bank.repository.TradeRepository;
import com.megha.bank.util.MTMessageParser;
import com.megha.bank.util.MTResponseBuilder;
import com.megha.bank.messaging.MessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * TradeService processes incoming MT messages, persists trades, generates MT-style FIN responses (ACK/NAK),
 * records audit entries, and publishes responses to a message publisher (e.g., RabbitMQ) if configured.
 */
@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final MTMessageParser parser;

    @Autowired(required = false)
    private AuditService auditService;

    @Autowired(required = false)
    private MessagePublisher messagePublisher;

    @Autowired
    public TradeService(TradeRepository tradeRepository, MTMessageParser parser) {
        this.tradeRepository = tradeRepository;
        this.parser = parser;
    }

    @Transactional
    public ProcessResult processMtMessage(String mtMessage, Trade.Type expectedType) {
        // Parse MT message into structured DTO
        MTMessageParser.ParsedMT parsed = parser.parse(mtMessage);

        // Basic validation: ensure message type matches expectedType
        if (parsed.type == null || !parsed.type.equalsIgnoreCase(expectedType.name())) {
            parsed.type = expectedType.name();
        }

        // Origin reference (if provided) - often field ORIG_REF or RELATED_REF in inbound message, fallback to null
        String originRef = parsed.others.getOrDefault("ORIG_REF", null);

        // Simple validation rules for BUY/SELL
        if (expectedType == Trade.Type.BUY || expectedType == Trade.Type.SELL) {
            if (parsed.instrument == null || parsed.quantity == null || parsed.price == null) {
                String nak = MTResponseBuilder.nak(parsed.reference, "MISSING_FIELDS: instrument/quantity/price", originRef);
                // Audit and publish NAK
                if (auditService != null) {
                    auditService.record(parsed.reference, mtMessage, nak, "NAK");
                }
                if (messagePublisher != null) {
                    messagePublisher.publish("mt.acks", nak);
                }
                return new ProcessResult(null, nak, false);
            }
        }

        Trade trade;
        if (expectedType == Trade.Type.CANCEL) {
            // For cancel, find original trade by reference and mark cancelled
            String ref = parsed.reference;
            if (ref != null) {
                trade = tradeRepository.findByTradeRef(ref)
                        .map(t -> {
                            t.setStatus(Trade.Status.CANCELLED);
                            t.setUpdatedAt(LocalDateTime.now());
                            return tradeRepository.save(t);
                        }).orElseGet(() -> {
                            // If not found, create a cancel record with reference
                            Trade c = new Trade();
                            c.setTradeRef(ref);
                            c.setType(Trade.Type.CANCEL);
                            c.setStatus(Trade.Status.CANCELLED);
                            c.setInstrument(parsed.instrument);
                            c.setQuantity(parsed.quantity);
                            c.setPrice(parsed.price);
                            c.setCreatedAt(LocalDateTime.now());
                            c.setUpdatedAt(LocalDateTime.now());
                            return tradeRepository.save(c);
                        });
            } else {
                // No reference provided: create a cancel placeholder
                Trade c = new Trade();
                c.setTradeRef(generateRef());
                c.setType(Trade.Type.CANCEL);
                c.setStatus(Trade.Status.CANCELLED);
                c.setInstrument(parsed.instrument);
                c.setQuantity(parsed.quantity);
                c.setPrice(parsed.price);
                c.setCreatedAt(LocalDateTime.now());
                c.setUpdatedAt(LocalDateTime.now());
                trade = tradeRepository.save(c);
            }
            String ack = MTResponseBuilder.ack(trade.getTradeRef(), "CANCELLED", originRef);
            // Audit and publish ACK
            if (auditService != null) {
                auditService.record(trade.getTradeRef(), mtMessage, ack, "ACK");
            }
            if (messagePublisher != null) {
                messagePublisher.publish("mt.acks", ack);
            }
            return new ProcessResult(trade, ack, true);
        } else {
            trade = new Trade();
            trade.setTradeRef(parsed.reference == null ? generateRef() : parsed.reference);
            trade.setType(expectedType);
            trade.setInstrument(parsed.instrument);
            trade.setQuantity(parsed.quantity);
            trade.setPrice(parsed.price);
            trade.setStatus(Trade.Status.NEW);
            trade.setCreatedAt(LocalDateTime.now());
            trade.setUpdatedAt(LocalDateTime.now());
            trade = tradeRepository.save(trade);

            String ack = MTResponseBuilder.ack(trade.getTradeRef(), "RECEIVED", originRef);
            // Audit and publish ACK
            if (auditService != null) {
                auditService.record(trade.getTradeRef(), mtMessage, ack, "ACK");
            }
            if (messagePublisher != null) {
                messagePublisher.publish("mt.acks", ack);
            }
            return new ProcessResult(trade, ack, true);
        }
    }

    private String generateRef() {
        return "TB-" + System.currentTimeMillis();
    }

    public static class ProcessResult {
        private final Trade trade;
        private final String mtResponse;
        private final boolean success;

        public ProcessResult(Trade trade, String mtResponse, boolean success) {
            this.trade = trade;
            this.mtResponse = mtResponse;
            this.success = success;
        }

        public Trade getTrade() { return trade; }
        public String getMtResponse() { return mtResponse; }
        public boolean isSuccess() { return success; }
    }
}
