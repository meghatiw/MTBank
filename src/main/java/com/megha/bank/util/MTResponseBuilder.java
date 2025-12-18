package com.megha.bank.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Builds SWIFT-like FIN messages (MT199) for ACK and NAK in a simplified format.
 * Note: This is a PoC representation and not a production-grade SWIFT FIN builder.
 *
 * Example output (simplified):
 * {@code
 * {1:F01MEGHABANKXXXX0000000000}{2:I199MEGHABANKXXXXN}{4:
 * :20:REF
 * :21:ORIGREF
 * :30:20251127
 * :79:ACK - STATUS:RECEIVED
 * -}
 * }
 */
public final class MTResponseBuilder {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private MTResponseBuilder() {
        // utility class
    }

    /**
     * Construct a simplified MT199 FIN message as ACK.
     *
     * @param originalReference sender's original reference (will be placed in :20:)
     * @param status            short status string included in narrative
     * @param originReference   related/origin reference (placed in :21:)
     * @return formatted simplified MT199 message
     */
    public static String ack(String originalReference, String status, String originReference) {
        String ref = originalReference == null ? "-" : originalReference;
        String orig = originReference == null ? "-" : originReference;
        String date = LocalDate.now().format(DATE_FMT);

        StringBuilder sb = new StringBuilder(256);
        sb.append("{1:F01MEGHABANKXXXX0000000000}"); // Basic header (example)
        sb.append("{2:I199MEGHABANKXXXXN}"); // Application/MT type 199
        sb.append("{4:\n");
        sb.append(":20:").append(ref).append("\n");   // Sender's reference
        sb.append(":21:").append(orig).append("\n");  // Related/origin reference
        sb.append(":30:").append(date).append("\n");  // Date
        sb.append(":79:").append("ACK - STATUS:").append(safeField(status)).append("\n"); // Narrative
        sb.append("-}");
        return sb.toString();
    }

    /**
     * Construct a simplified MT199 FIN message as NAK / Error.
     *
     * @param originalReference sender's original reference
     * @param reason            error reason (will be sanitized)
     * @param originReference   related/origin reference
     * @return formatted simplified MT199 NAK message
     */
    public static String nak(String originalReference, String reason, String originReference) {
        String ref = originalReference == null ? "-" : originalReference;
        String orig = originReference == null ? "-" : originReference;
        String date = LocalDate.now().format(DATE_FMT);
        String safeReason = reason == null ? "UNKNOWN" : sanitizeReason(reason);

        StringBuilder sb = new StringBuilder(256);
        sb.append("{1:F01MEGHABANKXXXX0000000000}");
        sb.append("{2:I199MEGHABANKXXXXN}");
        sb.append("{4:\n");
        sb.append(":20:").append(ref).append("\n");
        sb.append(":21:").append(orig).append("\n");
        sb.append(":30:").append(date).append("\n");
        sb.append(":79:").append("NAK - ERR:").append(safeReason).append("\n");
        sb.append("-}");
        return sb.toString();
    }

    private static String sanitizeReason(String r) {
        // remove newline chars and problematic braces/pipes that might break formatting
        return r.replaceAll("\\r?\\n", " ")
                .replaceAll("[\\{\\}]", " ")
                .replaceAll("\\|", ";")
                .trim();
    }

    private static String safeField(String s) {
        if (s == null) return "-";
        return s.replaceAll("\\r?\\n", " ").replaceAll("[\\{\\}]", " ").replaceAll("\\|", ";").trim();
    }
}
