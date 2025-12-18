# MeghaBank Middle Office - Trade Processor (PoC)

This Spring Boot application exposes three REST endpoints to accept MT-format trade messages (BUY, SELL, CANCEL), parses the incoming text, saves trade records to an H2 database, and returns the stored trade as JSON.

## Endpoints

- POST /api/trade/buy
- POST /api/trade/sell
- POST /api/trade/cancel

Each endpoint accepts a raw text body in a simple key:value MT format, for example:

```
TYPE:BUY
REF:TR-1001
INST:INFY
QTY:100
PRC:123.45
```

## Run

Requirements: Java 17 and Maven installed.

1. Build: `mvn clean package`
2. Run: `java -jar target/middleoffice-trades-0.0.1-SNAPSHOT.jar`
3. H2 console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:meghabankdb)

## Sample curl

Create BUY:
```
curl -X POST http://localhost:8080/api/trade/buy -d $'TYPE:BUY\nREF:TR-1001\nINST:INFY\nQTY:100\nPRC:123.45' -H "Content-Type: text/plain"
```

Create SELL:
```
curl -X POST http://localhost:8080/api/trade/sell -d $'TYPE:SELL\nREF:TR-1002\nINST:TCS\nQTY:50\nPRC:2500.00' -H "Content-Type: text/plain"
```

Cancel:
```
curl -X POST http://localhost:8080/api/trade/cancel -d $'TYPE:CANCEL\nREF:TR-1001' -H "Content-Type: text/plain"
```

## Notes

- This is a PoC parser; production SWIFT MT handling requires dedicated parsers and strict validation, plus security, audit, and transactional guarantees.
- Next steps: add validation, ACK/NAK response messages in MT format, enrich parser to actual SWIFT field tags, message signing and authentication, audit logging, and integration with message queue (MQ) or bank internal bus.
