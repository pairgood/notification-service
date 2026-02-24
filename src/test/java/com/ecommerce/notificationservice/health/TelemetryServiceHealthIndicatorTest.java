package com.ecommerce.notificationservice.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryServiceHealthIndicatorTest {

    @Test
    void shouldReturnUpWhenTelemetryServiceIsReachable() {
        TelemetryServiceHealthIndicator indicator = new TelemetryServiceHealthIndicator();
        ReflectionTestUtils.setField(indicator, "telemetryServiceUrl", "http://localhost:9999");

        Health health = indicator.health(); // Will return DOWN in unit test — no server running
        assertThat(health).isNotNull();
        assertThat(health.getDetails()).containsKey("url");
        assertThat(health.getDetails()).containsKey("responseTimeMs");
    }

    @Test
    void shouldReturnDownWhenTelemetryServiceIsUnreachable() {
        TelemetryServiceHealthIndicator indicator = new TelemetryServiceHealthIndicator();
        ReflectionTestUtils.setField(indicator, "telemetryServiceUrl", "http://localhost:1"); // port 1 always refused

        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
        assertThat(health.getDetails()).containsKey("responseTimeMs");
    }

    @Test
    void shouldIncludeUrlInDetails() {
        TelemetryServiceHealthIndicator indicator = new TelemetryServiceHealthIndicator();
        String url = "http://localhost:1";
        ReflectionTestUtils.setField(indicator, "telemetryServiceUrl", url);

        Health health = indicator.health();
        assertThat(health.getDetails().get("url")).isEqualTo(url);
    }
}
