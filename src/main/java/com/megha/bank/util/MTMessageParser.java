package com.megha.bank.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MTMessageParser {

    // Very simple example parser for a custom simplified "MT" text format.
    // Real SWIFT MT messages are more complex and require proper field parsing.

    public static class ParsedMT {
        public String type; // e.g., BUY, SELL, CANCEL
        public String reference; // trade reference
        public String instrument; // e.g., ABCXYZ
        public Long quantity;
        public Double price;
        public Map<String, String> others = new HashMap<>();
    }

    public ParsedMT parse(String mtRaw) {
        ParsedMT p = new ParsedMT();
        if (mtRaw == null) return p;
        // For this PoC, assume messages are in key:value lines, e.g.:
        // TYPE:BUY\nREF:TR123\nINST:INFY\nQTY:100\nPRC:123.45
        String[] lines = mtRaw.split("\\r?\\n");
        for (String l : lines) {
            String line = l.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(":", 2);
            if (parts.length < 2) continue;
            String k = parts[0].trim().toUpperCase();
            String v = parts[1].trim();
            switch (k) {
                case "TYPE": p.type = v; break;
                case "REF": p.reference = v; break;
                case "INST": p.instrument = v; break;
                case "QTY": try { p.quantity = Long.parseLong(v); } catch (Exception e) { p.quantity = null; } break;
                case "PRC": try { p.price = Double.parseDouble(v); } catch (Exception e) { p.price = null; } break;
                default: p.others.put(k, v); break;
            }
        }
        return p;
    }
}
