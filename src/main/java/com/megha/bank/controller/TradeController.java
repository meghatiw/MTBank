package com.megha.bank.controller;

import com.megha.bank.model.Trade;
import com.megha.bank.service.TradeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trade")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    // Endpoint to receive a BUY MT message (raw MT format text in body)
    @PostMapping(value = "/buy", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> buy(@RequestBody String mtMessage) {
        TradeService.ProcessResult res = tradeService.processMtMessage(mtMessage, Trade.Type.BUY);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-MT-RESPONSE", res.getMtResponse());
        return ResponseEntity.ok().headers(headers).body(new ApiResponse(res.getTrade(), res.getMtResponse(), res.isSuccess()));
    }

    // Endpoint to receive a SELL MT message
    @PostMapping(value = "/sell", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> sell(@RequestBody String mtMessage) {
        TradeService.ProcessResult res = tradeService.processMtMessage(mtMessage, Trade.Type.SELL);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-MT-RESPONSE", res.getMtResponse());
        return ResponseEntity.ok().headers(headers).body(new ApiResponse(res.getTrade(), res.getMtResponse(), res.isSuccess()));
    }

    // Endpoint to receive a CANCEL MT message
    @PostMapping(value = "/cancel", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> cancel(@RequestBody String mtMessage) {
        TradeService.ProcessResult res = tradeService.processMtMessage(mtMessage, Trade.Type.CANCEL);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-MT-RESPONSE", res.getMtResponse());
        return ResponseEntity.ok().headers(headers).body(new ApiResponse(res.getTrade(), res.getMtResponse(), res.isSuccess()));
    }

    public static class ApiResponse {
        private final Trade trade;
        private final String mtResponse;
        private final boolean success;

        public ApiResponse(Trade trade, String mtResponse, boolean success) {
            this.trade = trade;
            this.mtResponse = mtResponse;
            this.success = success;
        }

        public Trade getTrade() { return trade; }
        public String getMtResponse() { return mtResponse; }
        public boolean isSuccess() { return success; }
    }
}
