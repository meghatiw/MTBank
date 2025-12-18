package com.megha.bank.service;

import com.megha.bank.model.Trade;
import com.megha.bank.repository.TradeRepository;
import com.megha.bank.util.MTMessageParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TradeServiceTest {

    private TradeRepository tradeRepository;
    private MTMessageParser parser;
    private TradeService tradeService;

    @BeforeEach
    public void setup() {
        tradeRepository = mock(TradeRepository.class);
        parser = new MTMessageParser();
        tradeService = new TradeService(tradeRepository, parser);
    }

    @Test
    public void processBuy_createsTradeAndSaves() {
        String msg = "TYPE:BUY\nREF:TR-200\nINST:TCS\nQTY:50\nPRC:2500.00";
        when(tradeRepository.save(any(Trade.class))).thenAnswer(i -> i.getArgument(0));

        TradeService.ProcessResult res = tradeService.processMtMessage(msg, Trade.Type.BUY);
        Trade t = res.getTrade();

        assertNotNull(t.getTradeRef());
        assertEquals(Trade.Type.BUY, t.getType());
        assertEquals("TCS", t.getInstrument());
        assertEquals(50L, t.getQuantity());
        assertEquals(2500.00, t.getPrice());
        assertTrue(res.isSuccess());
        assertNotNull(res.getMtResponse());
        verify(tradeRepository, times(1)).save(any(Trade.class));
    }

    @Test
    public void processCancel_marksExistingTradeCancelled() {
        String msg = "TYPE:CANCEL\nREF:TR-300";
        Trade existing = new Trade();
        existing.setId(1L);
        existing.setTradeRef("TR-300");
        existing.setStatus(Trade.Status.NEW);

        when(tradeRepository.findByTradeRef("TR-300")).thenReturn(Optional.of(existing));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(i -> i.getArgument(0));

        TradeService.ProcessResult res = tradeService.processMtMessage(msg, Trade.Type.CANCEL);

        assertEquals(Trade.Status.CANCELLED, res.getTrade().getStatus());
        assertTrue(res.isSuccess());
        assertNotNull(res.getMtResponse());
        verify(tradeRepository, times(1)).findByTradeRef("TR-300");
        verify(tradeRepository, times(1)).save(existing);
    }
}
