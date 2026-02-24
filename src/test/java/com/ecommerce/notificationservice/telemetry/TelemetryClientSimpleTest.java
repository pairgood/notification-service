package com.ecommerce.notificationservice.telemetry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryClientSimpleTest {

    private TelemetryClient telemetryClient;

    @BeforeEach
    void setUp() {
        telemetryClient = new TelemetryClient(WebClient.builder());
        ReflectionTestUtils.setField(telemetryClient, "telemetryServiceUrl", "http://localhost:8086");
        ReflectionTestUtils.setField(telemetryClient, "serviceName", "notification-service");
        
        // Clear any existing trace context
        TelemetryClient.TraceContext.clear();
    }

    @Test
    void startTrace_ShouldGenerateTraceIdAndSetContext() {
        String traceId = telemetryClient.startTrace("test_operation", "POST", "/api/test", "user123");

        assertThat(traceId).isNotNull();
        assertThat(traceId).startsWith("trace_");
        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo(traceId);
        assertThat(TelemetryClient.TraceContext.getSpanId()).isNotNull();
        assertThat(TelemetryClient.TraceContext.getSpanId()).startsWith("span_");
        assertThat(TelemetryClient.TraceContext.getStartTime()).isNotNull();
    }

    @Test
    void startTrace_ShouldHandleNullUserId() {
        String traceId = telemetryClient.startTrace("test_operation", "GET", "/api/test", null);

        assertThat(traceId).isNotNull();
        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo(traceId);
    }

    @Test
    void finishTrace_ShouldClearContext() {
        // First set up a trace context
        TelemetryClient.TraceContext.setTraceId("test_trace");
        TelemetryClient.TraceContext.setSpanId("test_span");
        TelemetryClient.TraceContext.setStartTime(System.currentTimeMillis() - 1000);

        telemetryClient.finishTrace("test_operation", 200, null);

        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
        assertThat(TelemetryClient.TraceContext.getSpanId()).isNull();
        assertThat(TelemetryClient.TraceContext.getStartTime()).isNull();
    }

    @Test
    void finishTrace_ShouldHandleNoActiveTrace() {
        // Ensure no active trace
        TelemetryClient.TraceContext.clear();

        // This should not throw an exception
        telemetryClient.finishTrace("test_operation", 200, null);

        // Context should remain clear
        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
    }

    @Test
    void finishTrace_ShouldHandleErrorStatus() {
        TelemetryClient.TraceContext.setTraceId("test_trace");
        TelemetryClient.TraceContext.setSpanId("test_span");
        TelemetryClient.TraceContext.setStartTime(System.currentTimeMillis() - 1000);

        telemetryClient.finishTrace("test_operation", 500, "Internal server error");

        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
    }

    @Test
    void recordServiceCall_ShouldHandleNoActiveTrace() {
        TelemetryClient.TraceContext.clear();

        // This should not throw an exception
        telemetryClient.recordServiceCall("email-service", "send_email", "POST", "/send", 150, 200);

        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
    }

    @Test
    void recordServiceCall_ShouldMaintainContext() {
        TelemetryClient.TraceContext.setTraceId("parent_trace");
        TelemetryClient.TraceContext.setSpanId("parent_span");

        telemetryClient.recordServiceCall("email-service", "send_email", "POST", "/send", 150, 200);

        // Context should remain set
        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo("parent_trace");
        assertThat(TelemetryClient.TraceContext.getSpanId()).isEqualTo("parent_span");
    }

    @Test
    void logEvent_ShouldCreateLogEvent() {
        TelemetryClient.TraceContext.setTraceId("test_trace");
        TelemetryClient.TraceContext.setSpanId("test_span");

        telemetryClient.logEvent("User notification sent successfully", "INFO");

        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo("test_trace");
    }

    @Test
    void logEvent_ShouldHandleNoActiveTrace() {
        TelemetryClient.TraceContext.clear();

        // This should not throw an exception
        telemetryClient.logEvent("Test log message", "DEBUG");

        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
    }

    @Test
    void traceContext_ShouldHandleThreadLocalOperations() {
        String testTraceId = "test_trace_123";
        String testSpanId = "test_span_456";
        Long testStartTime = System.currentTimeMillis();

        TelemetryClient.TraceContext.setTraceId(testTraceId);
        TelemetryClient.TraceContext.setSpanId(testSpanId);
        TelemetryClient.TraceContext.setStartTime(testStartTime);

        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo(testTraceId);
        assertThat(TelemetryClient.TraceContext.getSpanId()).isEqualTo(testSpanId);
        assertThat(TelemetryClient.TraceContext.getStartTime()).isEqualTo(testStartTime);

        TelemetryClient.TraceContext.clear();

        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
        assertThat(TelemetryClient.TraceContext.getSpanId()).isNull();
        assertThat(TelemetryClient.TraceContext.getStartTime()).isNull();
    }

    @Test
    void traceContext_ShouldPropagateCorrectly() {
        // Clear any existing context first
        TelemetryClient.TraceContext.clear();
        
        String propagatedTraceId = "propagated_trace";
        String propagatedSpanId = "propagated_span";

        TelemetryClient.TraceContext.propagate(propagatedTraceId, propagatedSpanId);

        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo(propagatedTraceId);
        assertThat(TelemetryClient.TraceContext.getSpanId()).isEqualTo(propagatedSpanId);
    }

    @Test
    void startTrace_ShouldGenerateValidTraceIdFormat() {
        String traceId = telemetryClient.startTrace("test", "GET", "/test", null);

        assertThat(traceId).startsWith("trace_");
        assertThat(traceId).doesNotContain("-");
        assertThat(traceId).hasSize(38); // "trace_" + 32 characters
    }

    @Test
    void startTrace_ShouldGenerateValidSpanIdFormat() {
        telemetryClient.startTrace("test", "GET", "/test", null);

        String spanId = TelemetryClient.TraceContext.getSpanId();
        assertThat(spanId).startsWith("span_");
        assertThat(spanId.length()).isGreaterThan(5); // "span_" + hex characters
    }

    @Test
    void startTrace_ShouldHandleEmptyStrings() {
        String traceId = telemetryClient.startTrace("", "", "", "");

        assertThat(traceId).isNotNull();
        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo(traceId);
    }

    @Test
    void recordServiceCall_ShouldHandleZeroDuration() {
        TelemetryClient.TraceContext.setTraceId("test_trace");
        TelemetryClient.TraceContext.setSpanId("test_span");

        telemetryClient.recordServiceCall("test-service", "test_op", "GET", "/test", 0, 200);

        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo("test_trace");
    }

    @Test
    void logEvent_ShouldHandleNullMessage() {
        TelemetryClient.TraceContext.setTraceId("test_trace");
        TelemetryClient.TraceContext.setSpanId("test_span");

        telemetryClient.logEvent(null, "ERROR");

        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo("test_trace");
    }

    @Test
    void finishTrace_ShouldHandleNullStartTime() {
        TelemetryClient.TraceContext.setTraceId("test_trace");
        TelemetryClient.TraceContext.setSpanId("test_span");
        // Don't set start time

        telemetryClient.finishTrace("test_operation", 200, null);

        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
    }
}