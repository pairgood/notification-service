package com.ecommerce.notificationservice.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.ecommerce.notificationservice.telemetry.TelemetryClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "telemetry-service")
class TelemetryServiceConsumerPactTest {

    private TelemetryClient telemetryClient;

    @BeforeEach
    void setUp() {
        telemetryClient = new TelemetryClient();
        ReflectionTestUtils.setField(telemetryClient, "serviceName", "notification-service");
        TelemetryClient.TraceContext.clear();
    }

    @AfterEach
    void tearDown() {
        TelemetryClient.TraceContext.clear();
    }

    @Pact(consumer = "notification-service", provider = "telemetry-service")
    public V4Pact startTraceInteraction(PactBuilder builder) {
        return builder
                .expectsToReceiveHttpInteraction("a start trace event", interaction -> interaction
                    .withRequest(request -> request
                        .method("POST")
                        .path("/api/telemetry/events")
                        .body(LambdaDsl.newJsonBody(body -> {
                            body.stringType("traceId", "trace_example");
                            body.stringType("spanId", "span_example");
                            body.stringValue("serviceName", "notification-service");
                            body.stringValue("operation", "send_order_confirmation");
                            body.stringValue("eventType", "SPAN");
                            body.minArrayLike("timestamp", 7, PactDslJsonRootValue.integerType(2024), 7);
                            body.stringValue("status", "SUCCESS");
                            body.stringValue("httpMethod", "POST");
                            body.stringValue("httpUrl", "/api/notifications/order-confirmation");
                            body.stringValue("userId", "123");
                        }).build())
                    )
                    .willRespondWith(response -> response
                        .status(202)
                    )
                )
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "startTraceInteraction")
    void testStartTrace(MockServer mockServer) throws InterruptedException {
        // Configure TelemetryClient to use mock server
        ReflectionTestUtils.setField(telemetryClient, "telemetryServiceUrl", mockServer.getUrl());
        ReflectionTestUtils.setField(telemetryClient, "webClient", WebClient.builder().build());
        
        // Call startTrace
        String traceId = telemetryClient.startTrace(
            "send_order_confirmation", 
            "POST", 
            "/api/notifications/order-confirmation", 
            "123"
        );

        // Wait for WebClient to complete the async request
        Thread.sleep(1000);

        // Verify trace context was set
        assertThat(traceId).isNotNull();
        assertThat(traceId).startsWith("trace_");
        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo(traceId);
        assertThat(TelemetryClient.TraceContext.getSpanId()).isNotNull();
    }

    @Pact(consumer = "notification-service", provider = "telemetry-service")
    public V4Pact finishTraceInteraction(PactBuilder builder) {
        return builder
                .expectsToReceiveHttpInteraction("a finish trace event with success", interaction -> interaction
                    .withRequest(request -> request
                        .method("POST")
                        .path("/api/telemetry/events")
                        .body(LambdaDsl.newJsonBody(body -> {
                            body.stringType("traceId", "trace_example");
                            body.stringType("spanId", "span_example");
                            body.stringValue("serviceName", "notification-service");
                            body.stringValue("operation", "send_order_confirmation_complete");
                            body.stringValue("eventType", "SPAN");
                            body.minArrayLike("timestamp", 7, PactDslJsonRootValue.integerType(2024), 7);
                            body.numberType("durationMs", 150);
                            body.stringValue("status", "SUCCESS");
                            body.numberType("httpStatusCode", 200);
                            body.stringValue("errorMessage", "");
                        }).build())
                    )
                    .willRespondWith(response -> response
                        .status(202)
                    )
                )
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "finishTraceInteraction")
    void testFinishTrace(MockServer mockServer) throws InterruptedException {
        // Configure TelemetryClient to use mock server
        ReflectionTestUtils.setField(telemetryClient, "telemetryServiceUrl", mockServer.getUrl());
        ReflectionTestUtils.setField(telemetryClient, "webClient", WebClient.builder().build());

        // Set up trace context
        TelemetryClient.TraceContext.setTraceId("trace_abc123");
        TelemetryClient.TraceContext.setSpanId("span_def456");
        TelemetryClient.TraceContext.setStartTime(System.currentTimeMillis() - 150);

        // Call finishTrace
        telemetryClient.finishTrace("send_order_confirmation", 200, null);

        // Wait for WebClient to complete the async request
        Thread.sleep(1000);

        // Verify trace context was cleared
        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
        assertThat(TelemetryClient.TraceContext.getSpanId()).isNull();
    }

    @Pact(consumer = "notification-service", provider = "telemetry-service")
    public V4Pact recordServiceCallInteraction(PactBuilder builder) {
        return builder
                .expectsToReceiveHttpInteraction("a service call event", interaction -> interaction
                    .withRequest(request -> request
                        .method("POST")
                        .path("/api/telemetry/events")
                        .body(LambdaDsl.newJsonBody(body -> {
                            body.stringType("traceId", "trace_example");
                            body.stringType("spanId", "span_example");
                            body.stringType("parentSpanId", "parent_span_example");
                            body.stringValue("serviceName", "notification-service");
                            body.stringValue("operation", "email-provider_send_email");
                            body.stringValue("eventType", "SPAN");
                            body.minArrayLike("timestamp", 7, PactDslJsonRootValue.integerType(2024), 7);
                            body.numberType("durationMs", 520);
                            body.stringValue("status", "SUCCESS");
                            body.stringValue("httpMethod", "POST");
                            body.stringValue("httpUrl", "smtp://email.service");
                            body.numberType("httpStatusCode", 200);
                            body.stringValue("metadata", "Outbound call to email-provider");
                        }).build())
                    )
                    .willRespondWith(response -> response
                        .status(202)
                    )
                )
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "recordServiceCallInteraction")
    void testRecordServiceCall(MockServer mockServer) throws InterruptedException {
        // Configure TelemetryClient to use mock server
        ReflectionTestUtils.setField(telemetryClient, "telemetryServiceUrl", mockServer.getUrl());
        ReflectionTestUtils.setField(telemetryClient, "webClient", WebClient.builder().build());

        // Set up trace context
        TelemetryClient.TraceContext.setTraceId("trace_abc123");
        TelemetryClient.TraceContext.setSpanId("span_def456");

        // Call recordServiceCall
        telemetryClient.recordServiceCall(
            "email-provider",
            "send_email",
            "POST",
            "smtp://email.service",
            520,
            200
        );

        // Wait for WebClient to complete the async request
        Thread.sleep(1000);

        // Verify trace context still exists (not cleared)
        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo("trace_abc123");
        assertThat(TelemetryClient.TraceContext.getSpanId()).isEqualTo("span_def456");
    }
}
