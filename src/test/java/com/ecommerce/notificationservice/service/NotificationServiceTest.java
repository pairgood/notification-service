package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.model.Notification;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import com.ecommerce.notificationservice.telemetry.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;
    
    @Mock
    private TelemetryClient telemetryClient;

    @InjectMocks
    private NotificationService notificationService;

    private Notification savedNotification;

    @BeforeEach
    void setUp() {
        savedNotification = new Notification(
            1L,
            Notification.NotificationType.ORDER_CONFIRMATION,
            "Order Confirmed",
            "Your order has been confirmed"
        );
        savedNotification.setId(100L);
    }

    @Test
    void sendOrderConfirmation_ShouldCreateAndSendNotification() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendOrderConfirmation(123L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(Notification.NotificationType.ORDER_CONFIRMATION);
        assertThat(result.getSubject()).contains("Order #123");
        assertThat(result.getMessage()).contains("order #123 has been confirmed");
        assertThat(result.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
        assertThat(result.getSentAt()).isNotNull();

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendNotification(any(Notification.class));
    }

    @Test
    void sendOrderConfirmation_ShouldHandleEmailServiceFailure() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doThrow(new RuntimeException("Email service unavailable"))
            .when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendOrderConfirmation(123L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Notification.NotificationStatus.FAILED);
        assertThat(result.getSentAt()).isNull();

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendNotification(any(Notification.class));
    }

    @Test
    void sendOrderStatusUpdate_ShouldCreateNotificationWithCorrectContent() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendOrderStatusUpdate(456L, 2L, "SHIPPED");

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(Notification.NotificationType.ORDER_STATUS_UPDATE);
        assertThat(result.getSubject()).contains("Order Status Update - Order #456");
        assertThat(result.getMessage()).contains("order #456 status has been updated to: SHIPPED");
        assertThat(result.getUserId()).isEqualTo(2L);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendNotification(any(Notification.class));
    }

    @Test
    void sendOrderCancellation_ShouldCreateNotificationWithCorrectContent() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendOrderCancellation(789L, 3L);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(Notification.NotificationType.ORDER_CANCELLATION);
        assertThat(result.getSubject()).contains("Order Cancelled - Order #789");
        assertThat(result.getMessage()).contains("order #789 has been cancelled");
        assertThat(result.getMessage()).contains("refund will be processed within 3-5 business days");
        assertThat(result.getUserId()).isEqualTo(3L);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendNotification(any(Notification.class));
    }

    @Test
    void sendPaymentConfirmation_ShouldCreateNotificationWithCorrectContent() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendPaymentConfirmation(999L, 4L, 555L);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(Notification.NotificationType.PAYMENT_CONFIRMATION);
        assertThat(result.getSubject()).contains("Payment Confirmation - Order #555");
        assertThat(result.getMessage()).contains("payment has been successfully processed for order #555");
        assertThat(result.getMessage()).contains("Payment ID: 999");
        assertThat(result.getUserId()).isEqualTo(4L);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendNotification(any(Notification.class));
    }

    @Test
    void sendPaymentFailure_ShouldCreateNotificationWithCorrectContent() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendPaymentFailure(888L, 5L, 777L);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(Notification.NotificationType.PAYMENT_FAILURE);
        assertThat(result.getSubject()).contains("Payment Failed - Order #777");
        assertThat(result.getMessage()).contains("Payment processing failed for order #777");
        assertThat(result.getMessage()).contains("Payment ID: 888");
        assertThat(result.getUserId()).isEqualTo(5L);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendNotification(any(Notification.class));
    }

    @Test
    void sendRefundConfirmation_ShouldCreateNotificationWithCorrectContent() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendRefundConfirmation(666L, 6L, 444L);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(Notification.NotificationType.REFUND_CONFIRMATION);
        assertThat(result.getSubject()).contains("Refund Processed - Order #444");
        assertThat(result.getMessage()).contains("refund has been processed for order #444");
        assertThat(result.getMessage()).contains("within 3-5 business days");
        assertThat(result.getMessage()).contains("Payment ID: 666");
        assertThat(result.getUserId()).isEqualTo(6L);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendNotification(any(Notification.class));
    }

    @Test
    void getNotificationById_ShouldReturnNotificationWhenExists() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(savedNotification));

        Notification result = notificationService.getNotificationById(100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result).isEqualTo(savedNotification);

        verify(notificationRepository).findById(100L);
    }

    @Test
    void getNotificationById_ShouldThrowExceptionWhenNotExists() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Notification not found");

        verify(notificationRepository).findById(999L);
    }

    @Test
    void getNotificationsByUserId_ShouldReturnUserNotifications() {
        Notification notification2 = new Notification(1L, Notification.NotificationType.PAYMENT_CONFIRMATION, "Payment", "Payment confirmed");
        notification2.setId(101L);
        
        List<Notification> expectedNotifications = Arrays.asList(savedNotification, notification2);
        when(notificationRepository.findByUserId(1L)).thenReturn(expectedNotifications);

        List<Notification> result = notificationService.getNotificationsByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedNotifications);
        assertThat(result).allMatch(notification -> notification.getUserId().equals(1L));

        verify(notificationRepository).findByUserId(1L);
    }

    @Test
    void getNotificationsByUserId_ShouldReturnEmptyListWhenNoNotifications() {
        when(notificationRepository.findByUserId(999L)).thenReturn(Arrays.asList());

        List<Notification> result = notificationService.getNotificationsByUserId(999L);

        assertThat(result).isEmpty();

        verify(notificationRepository).findByUserId(999L);
    }

    @Test
    void getAllNotifications_ShouldReturnAllNotifications() {
        Notification notification2 = new Notification(2L, Notification.NotificationType.ORDER_CANCELLATION, "Cancelled", "Order cancelled");
        notification2.setId(102L);
        
        List<Notification> expectedNotifications = Arrays.asList(savedNotification, notification2);
        when(notificationRepository.findAll()).thenReturn(expectedNotifications);

        List<Notification> result = notificationService.getAllNotifications();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedNotifications);

        verify(notificationRepository).findAll();
    }

    @Test
    void processNotification_ShouldSetSentStatusWhenEmailSucceeds() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendOrderConfirmation(123L, 1L);

        assertThat(result.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
        assertThat(result.getSentAt()).isNotNull();
    }

    @Test
    void processNotification_ShouldSetFailedStatusWhenEmailFails() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doThrow(new RuntimeException("Email failed")).when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendOrderConfirmation(123L, 1L);

        assertThat(result.getStatus()).isEqualTo(Notification.NotificationStatus.FAILED);
        assertThat(result.getSentAt()).isNull();
    }

    @Test
    void sendOrderConfirmation_ShouldHandleNullValues() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendOrderConfirmation(null, null);

        assertThat(result).isNotNull();
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendNotification(any(Notification.class));
    }

    @Test
    void sendOrderStatusUpdate_ShouldHandleNullStatus() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendOrderStatusUpdate(123L, 1L, null);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("null");
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void processNotification_ShouldHandleRepositorySaveFailure() {
        when(notificationRepository.save(any(Notification.class)))
            .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> notificationService.sendOrderConfirmation(123L, 1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");

        verify(notificationRepository).save(any(Notification.class));
        verify(emailService, never()).sendNotification(any(Notification.class));
    }

    @Test
    void sendPaymentConfirmation_ShouldHandleZeroIds() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendPaymentConfirmation(0L, 0L, 0L);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("Payment ID: 0");
        assertThat(result.getMessage()).contains("order #0");
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendPaymentFailure_ShouldHandleNegativeIds() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Notification result = notificationService.sendPaymentFailure(-1L, -2L, -3L);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("Payment ID: -1");
        assertThat(result.getMessage()).contains("order #-3");
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendRefundConfirmation_ShouldHandleLargeIds() {
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification notification = invocation.getArgument(0);
                notification.setId(100L);
                return notification;
            });
        
        doNothing().when(emailService).sendNotification(any(Notification.class));

        Long largeId = Long.MAX_VALUE;
        Notification result = notificationService.sendRefundConfirmation(largeId, largeId, largeId);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("Payment ID: " + largeId);
        assertThat(result.getMessage()).contains("order #" + largeId);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
}