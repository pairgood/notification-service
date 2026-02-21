package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.model.Notification;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import com.ecommerce.notificationservice.telemetry.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private TelemetryClient telemetryClient;
    
    public Notification sendOrderConfirmation(Long orderId, Long userId) {
        String subject = "Order Confirmation - Order #" + orderId;
        String message = String.format(
            "Thank you for your order! Your order #%d has been confirmed and is being processed. " +
            "You will receive updates as your order progresses.", 
            orderId
        );
        
        Notification notification = new Notification(
            userId, 
            Notification.NotificationType.ORDER_CONFIRMATION, 
            subject, 
            message
        );
        
        return processNotification(notification);
    }
    
    public Notification sendOrderStatusUpdate(Long orderId, Long userId, String status) {
        String subject = "Order Status Update - Order #" + orderId;
        String message = String.format(
            "Your order #%d status has been updated to: %s", 
            orderId, status
        );
        
        Notification notification = new Notification(
            userId, 
            Notification.NotificationType.ORDER_STATUS_UPDATE, 
            subject, 
            message
        );
        
        return processNotification(notification);
    }
    
    public Notification sendOrderCancellation(Long orderId, Long userId) {
        String subject = "Order Cancelled - Order #" + orderId;
        String message = String.format(
            "Your order #%d has been cancelled. If you were charged, a refund will be processed within 3-5 business days.", 
            orderId
        );
        
        Notification notification = new Notification(
            userId, 
            Notification.NotificationType.ORDER_CANCELLATION, 
            subject, 
            message
        );
        
        return processNotification(notification);
    }
    
    public Notification sendPaymentConfirmation(Long paymentId, Long userId, Long orderId) {
        String subject = "Payment Confirmation - Order #" + orderId;
        String message = String.format(
            "Your payment has been successfully processed for order #%d. Payment ID: %d", 
            orderId, paymentId
        );
        
        Notification notification = new Notification(
            userId, 
            Notification.NotificationType.PAYMENT_CONFIRMATION, 
            subject, 
            message
        );
        
        return processNotification(notification);
    }
    
    public Notification sendPaymentFailure(Long paymentId, Long userId, Long orderId) {
        String subject = "Payment Failed - Order #" + orderId;
        String message = String.format(
            "Payment processing failed for order #%d. Please try again or use a different payment method. Payment ID: %d", 
            orderId, paymentId
        );
        
        Notification notification = new Notification(
            userId, 
            Notification.NotificationType.PAYMENT_FAILURE, 
            subject, 
            message
        );
        
        return processNotification(notification);
    }
    
    public Notification sendRefundConfirmation(Long paymentId, Long userId, Long orderId) {
        String subject = "Refund Processed - Order #" + orderId;
        String message = String.format(
            "Your refund has been processed for order #%d. You should see the refund in your account within 3-5 business days. Payment ID: %d", 
            orderId, paymentId
        );
        
        Notification notification = new Notification(
            userId, 
            Notification.NotificationType.REFUND_CONFIRMATION, 
            subject, 
            message
        );
        
        return processNotification(notification);
    }
    
    private Notification processNotification(Notification notification) {
        telemetryClient.logEvent("Processing notification: " + notification.getType(), "INFO");
        
        // Save notification to database
        notification = notificationRepository.save(notification);
        telemetryClient.logEvent("Notification saved to database with ID: " + notification.getId(), "INFO");
        
        try {
            // Send email notification
            emailService.sendNotification(notification);
            
            // Update notification status to sent
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            telemetryClient.logEvent("Notification sent successfully", "INFO");
            
        } catch (Exception e) {
            // If sending fails, mark as failed
            notification.setStatus(Notification.NotificationStatus.FAILED);
            telemetryClient.logEvent("Failed to send notification: " + e.getMessage(), "ERROR");
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        return notificationRepository.save(notification);
    }
    
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
    }
    
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}