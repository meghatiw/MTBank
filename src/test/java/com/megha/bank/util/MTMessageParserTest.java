package com.megha.bank.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MTMessageParserTest {

    @Test
    public void parse_validMessage_populatesFields() {
        String msg = "TYPE:BUY\nREF:TR-1001\nINST:INFY\nQTY:100\nPRC:123.45";
        MTMessageParser parser = new MTMessageParser();
        MTMessageParser.ParsedMT p = parser.parse(msg);

        assertEquals("BUY", p.type);
        assertEquals("TR-1001", p.reference);
        assertEquals("INFY", p.instrument);
        assertEquals(100L, p.quantity);
        assertEquals(123.45, p.price);
    }

    @Test
    public void parse_empty_returnsParsedObject() {
        MTMessageParser parser = new MTMessageParser();
        MTMessageParser.ParsedMT p = parser.parse(""); // no exception
        assertNotNull(p);
    }
}
