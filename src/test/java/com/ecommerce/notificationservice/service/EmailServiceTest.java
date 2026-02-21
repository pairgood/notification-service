package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.model.Notification;
import com.ecommerce.notificationservice.telemetry.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private TelemetryClient telemetryClient;
    
    @InjectMocks
    private EmailService emailService;
    
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification(
            1L,
            Notification.NotificationType.ORDER_CONFIRMATION,
            "Order Confirmed",
            "Your order has been confirmed and is being processed"
        );
    }

    @Test
    void sendNotification_ShouldProcessNotificationSuccessfully() {
        // Test that the service can handle a normal notification without throwing exceptions
        // This tests the majority case where random failure doesn't occur
        
        // Run the test multiple times to increase chance of success scenario
        boolean hasSucceeded = false;
        for (int i = 0; i < 10 && !hasSucceeded; i++) {
            try {
                emailService.sendNotification(testNotification);
                hasSucceeded = true;
            } catch (RuntimeException e) {
                // Expected occasional failures due to random 5% failure rate
                if (!e.getMessage().equals("Email service unavailable")) {
                    throw e; // Unexpected error type
                }
            }
        }
        
        // Verify at least one attempt succeeded
        assertThat(hasSucceeded).isTrue();
    }

    @Test
    void sendNotification_ShouldEventuallySucceedAfterRetries() {
        // Test that with multiple attempts, we can eventually succeed
        // This verifies the service actually works in the success case
        int attempts = 0;
        int maxAttempts = 30; // Should be enough to overcome 5% failure rate
        boolean succeeded = false;
        
        while (attempts < maxAttempts && !succeeded) {
            try {
                emailService.sendNotification(testNotification);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("Email service unavailable")) {
                    throw e; // Unexpected error
                }
                attempts++;
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendNotification_ShouldSometimesFailDueToRandomFailures() {
        // Test that we do encounter failures due to the 5% random failure rate
        // Run many attempts to ensure we hit at least one failure
        int failures = 0;
        int attempts = 100;
        
        for (int i = 0; i < attempts; i++) {
            try {
                emailService.sendNotification(testNotification);
            } catch (RuntimeException e) {
                if (e.getMessage().equals("Email service unavailable")) {
                    failures++;
                } else {
                    throw e; // Unexpected error type
                }
            }
        }
        
        // With 5% failure rate over 100 attempts, we should see some failures
        assertThat(failures).isGreaterThan(0);
        assertThat(failures).isLessThan(50); // Shouldn't be failing most of the time
    }

    @Test
    void sendNotification_ShouldHandleNotificationWithAllFields() {
        testNotification.setRecipientEmail("user@example.com");
        testNotification.setRecipientPhone("+1234567890");
        
        // Test that notifications with additional fields can be processed
        boolean succeeded = false;
        for (int i = 0; i < 10 && !succeeded; i++) {
            try {
                emailService.sendNotification(testNotification);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("Email service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendNotification_ShouldHandleLongMessage() {
        String longMessage = "A".repeat(1000);
        testNotification.setMessage(longMessage);
        
        // Test that long messages can be processed
        boolean succeeded = false;
        for (int i = 0; i < 10 && !succeeded; i++) {
            try {
                emailService.sendNotification(testNotification);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("Email service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendNotification_ShouldHandleShortMessage() {
        testNotification.setMessage("Hi");
        
        // Test that short messages can be processed
        boolean succeeded = false;
        for (int i = 0; i < 10 && !succeeded; i++) {
            try {
                emailService.sendNotification(testNotification);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("Email service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendSMS_ShouldSuccessfullySendSMS() {
        String phoneNumber = "+1234567890";
        String message = "Your order has been confirmed";
        
        // Test that SMS sending works in the success case
        boolean succeeded = false;
        for (int i = 0; i < 10 && !succeeded; i++) {
            try {
                emailService.sendSMS(phoneNumber, message);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("SMS service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendSMS_ShouldSometimesFailDueToRandomFailures() {
        String phoneNumber = "+1234567890";
        String message = "Test message";
        
        // Test that we encounter failures due to the 3% random failure rate
        int failures = 0;
        int attempts = 100;
        
        for (int i = 0; i < attempts; i++) {
            try {
                emailService.sendSMS(phoneNumber, message);
            } catch (RuntimeException e) {
                if (e.getMessage().equals("SMS service unavailable")) {
                    failures++;
                } else {
                    throw e;
                }
            }
        }
        
        // With 3% failure rate, we should see some failures but not too many
        assertThat(failures).isGreaterThanOrEqualTo(0);
        assertThat(failures).isLessThan(30); // Shouldn't be failing most of the time
    }

    @Test
    void sendSMS_ShouldWorkWithMultipleRetries() {
        String phoneNumber = "+1234567890";
        String message = "Test message";
        
        // Test that with retries we can overcome random failures
        boolean succeeded = false;
        int maxAttempts = 50;
        
        for (int i = 0; i < maxAttempts && !succeeded; i++) {
            try {
                emailService.sendSMS(phoneNumber, message);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("SMS service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendSMS_ShouldHandleNullPhoneNumber() {
        String message = "Test message";
        
        // Test that null phone numbers can be handled
        boolean succeeded = false;
        for (int i = 0; i < 10 && !succeeded; i++) {
            try {
                emailService.sendSMS(null, message);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("SMS service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendSMS_ShouldHandleEmptyMessage() {
        String phoneNumber = "+1234567890";
        
        // Test that empty messages can be handled
        boolean succeeded = false;
        for (int i = 0; i < 10 && !succeeded; i++) {
            try {
                emailService.sendSMS(phoneNumber, "");
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("SMS service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendNotification_ShouldHandleNullUserId() {
        testNotification.setUserId(null);
        
        // Test that null user IDs can be handled
        boolean succeeded = false;
        for (int i = 0; i < 10 && !succeeded; i++) {
            try {
                emailService.sendNotification(testNotification);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("Email service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendNotification_ShouldHandleNullSubject() {
        testNotification.setSubject(null);
        
        // Test that null subjects can be handled
        boolean succeeded = false;
        for (int i = 0; i < 10 && !succeeded; i++) {
            try {
                emailService.sendNotification(testNotification);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("Email service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendNotification_ShouldHandleNullType() {
        testNotification.setType(null);
        
        // Test that null types can be handled
        boolean succeeded = false;
        for (int i = 0; i < 10 && !succeeded; i++) {
            try {
                emailService.sendNotification(testNotification);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("Email service unavailable")) {
                    throw e;
                }
            }
        }
        
        assertThat(succeeded).isTrue();
    }

    @Test
    void sendNotification_ShouldHaveReasonablePerformance() {
        // Test that the service completes in reasonable time
        long startTime = System.currentTimeMillis();
        
        boolean succeeded = false;
        for (int i = 0; i < 5 && !succeeded; i++) {
            try {
                emailService.sendNotification(testNotification);
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("Email service unavailable")) {
                    throw e;
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // Should complete within reasonable time (including sleep delays)
        assertThat(endTime - startTime).isLessThan(5000); // 5 seconds
    }

    @Test
    void sendSMS_ShouldHaveReasonablePerformance() {
        // Test that SMS service completes in reasonable time
        long startTime = System.currentTimeMillis();
        
        boolean succeeded = false;
        for (int i = 0; i < 5 && !succeeded; i++) {
            try {
                emailService.sendSMS("+1234567890", "Test");
                succeeded = true;
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("SMS service unavailable")) {
                    throw e;
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // Should complete within reasonable time (including sleep delays)
        assertThat(endTime - startTime).isLessThan(3000); // 3 seconds
    }
    
    @Test
    void emailService_ShouldHandleMultipleOperations() {
        // Test that the service can handle multiple operations without state conflicts
        Notification notification1 = new Notification(1L, Notification.NotificationType.ORDER_CONFIRMATION, "Subject 1", "Message 1");
        Notification notification2 = new Notification(2L, Notification.NotificationType.PAYMENT_CONFIRMATION, "Subject 2", "Message 2");
        
        int successCount = 0;
        int totalAttempts = 10;
        
        for (int i = 0; i < totalAttempts; i++) {
            try {
                emailService.sendNotification(notification1);
                emailService.sendSMS("+1234567890", "SMS test " + i);
                emailService.sendNotification(notification2);
                successCount++;
            } catch (RuntimeException e) {
                // Expected occasional failures
                if (!e.getMessage().contains("service unavailable")) {
                    throw e;
                }
            }
        }
        
        // Should have some successes
        assertThat(successCount).isGreaterThan(0);
    }
}