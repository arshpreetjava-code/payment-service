# Payment Service

Short description

This service listens for `order-created` events and keeps pending payments for manual acknowledgement. It exposes an HTTP endpoint to manually acknowledge payments which will produce `payment-completed` or `payment-failed` events.

Key info
- Java: 21
- Spring Boot: 3.5.7
- Kafka topics used: `order-created`, `payment-completed`, `payment-failed`
- HTTP endpoints: `POST /api/payment/ack/{orderId}`
- Swagger UI (OpenAPI): `/swagger-ui.html` or `/swagger-ui/index.html`

Prerequisites
- Java 21 installed and JAVA_HOME set
- Maven (or use `mvnw.cmd`)
- Running Kafka cluster

Configuration
- Kafka bootstrap and group id are read from `src/main/resources/env.properties` (imported by `application.yml`).

Defaults in `env.properties`:

```
BOOTSTRAP_SERVER=ec2-13-201-72-156.ap-south-1.compute.amazonaws.com:9092
GROUP_ID=payment-service-group
```

- `application.yml` sets server port `8080` by default.

Build and run

Using Maven wrapper (Windows):

```bash
cd payment-service-main
mvnw.cmd -DskipTests package
java -jar target/payment-service-0.0.1-SNAPSHOT.jar
```

Swagger and API docs

- http://localhost:8080/v3/api-docs
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/swagger-ui/index.html

HTTP API

Acknowledge payment
- URL: POST /api/payment/ack/{orderId}
- Example:

```bash
curl -X POST http://localhost:8080/api/payment/ack/order-123
```

- Behavior: If a pending payment exists for `order-123`, the service publishes `payment-completed` and acknowledges the consumer offset. Otherwise it publishes `payment-failed`.

Kafka

- Listens to: `order-created`
- Publishes: `payment-completed` (on success), `payment-failed` (if no pending payment)

Troubleshooting

- If payments are not processed, ensure Kafka bootstrap server is correct and reachable.
- Check logs to see pending payments and ack operations.


