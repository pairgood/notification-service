package com.ecommerce.notificationservice.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import com.ecommerce.notificationservice.model.Notification;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import com.ecommerce.notificationservice.service.EmailService;
import com.ecommerce.notificationservice.telemetry.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Provider("notification-service")   // MUST match spring.application.name exactly
@PactBroker(
    url = "http://localhost:9292",
    authentication = @PactBrokerAuth(username = "admin", password = "admin")
)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationServiceProviderPactTest {

    @LocalServerPort
    private int port;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private TelemetryClient telemetryClient;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));

        // Set up default mock behavior for telemetry client
        when(telemetryClient.startTrace(anyString(), anyString(), anyString(), anyString())).thenReturn("trace_123");
        doNothing().when(telemetryClient).finishTrace(anyString(), anyInt(), anyString());
        doNothing().when(telemetryClient).logEvent(anyString(), anyString());
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    // State string must be IDENTICAL to consumer's given() — character for character
    @State("a user with id 42 exists")
    void userWithId42Exists() {
        // Mock the notification repository to save and return a notification
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                // Set an ID to simulate a saved entity
                notification.setId(1L);
                notification.setStatus(Notification.NotificationStatus.SENT);
                return notification;
            });

        // Mock the email service to not throw any exceptions
        doNothing().when(emailService).sendNotification(any(Notification.class));
    }
}
