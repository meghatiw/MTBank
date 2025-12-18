package com.megha.bank.controller;

import com.megha.bank.model.Trade;
import com.megha.bank.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TradeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    public void buyEndpoint_createsTrade_returnsJson() throws Exception {
        String body = "TYPE:BUY\nREF:TR-500\nINST:INFY\nQTY:100\nPRC:123.45";

        mockMvc.perform(post("/api/trade/buy")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(body))
                .andExpect(status().isOk())
                // Updated assertion: expect SWIFT-like FIN message in header containing the sender ref tag :20:TR-500
                .andExpect(header().string("X-MT-RESPONSE", containsString(":20:TR-500")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trade.tradeRef").value(containsString("TR-500")))
                .andExpect(jsonPath("$.trade.instrument").value("INFY"));
    }

    @Test
    public void cancelEndpoint_marksTradeCancelled() throws Exception {
        // First create a trade via buy endpoint
        String buy = "TYPE:BUY\nREF:TR-600\nINST:TCS\nQTY:10\nPRC:1200.0";
        mockMvc.perform(post("/api/trade/buy")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(buy))
                .andExpect(status().isOk());

        String cancel = "TYPE:CANCEL\nREF:TR-600";
        mockMvc.perform(post("/api/trade/cancel")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(cancel))
                .andExpect(status().isOk())
                // Updated assertion: expect SWIFT-like FIN header containing :20:TR-600
                .andExpect(header().string("X-MT-RESPONSE", containsString(":20:TR-600")))
                .andExpect(jsonPath("$.trade.status").value("CANCELLED"));
    }
}
