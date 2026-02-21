package com.ecommerce.notificationservice.controller;

import com.ecommerce.notificationservice.dto.*;
import com.ecommerce.notificationservice.model.Notification;
import com.ecommerce.notificationservice.service.NotificationService;
import com.ecommerce.notificationservice.telemetry.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private TelemetryClient telemetryClient;
    
    private String toUserId(Long userId) {
        return userId != null ? userId.toString() : null;
    }
    
    @PostMapping("/order-confirmation")
    public ResponseEntity<Notification> sendOrderConfirmation(@RequestBody OrderConfirmationRequest request) {
        telemetryClient.startTrace("send_order_confirmation", "POST", "/api/notifications/order-confirmation", toUserId(request.getUserId()));
        
        try {
            Notification notification = notificationService.sendOrderConfirmation(
                request.getOrderId(), request.getUserId()
            );
            telemetryClient.finishTrace("send_order_confirmation", 200, null);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            telemetryClient.finishTrace("send_order_confirmation", 500, e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/order-status")
    public ResponseEntity<Notification> sendOrderStatusUpdate(@RequestBody OrderStatusRequest request) {
        telemetryClient.startTrace("send_order_status_update", "POST", "/api/notifications/order-status", toUserId(request.getUserId()));
        
        try {
            Notification notification = notificationService.sendOrderStatusUpdate(
                request.getOrderId(), request.getUserId(), request.getStatus()
            );
            telemetryClient.finishTrace("send_order_status_update", 200, null);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            telemetryClient.finishTrace("send_order_status_update", 500, e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/order-cancellation")
    public ResponseEntity<Notification> sendOrderCancellation(@RequestBody OrderCancellationRequest request) {
        telemetryClient.startTrace("send_order_cancellation", "POST", "/api/notifications/order-cancellation", toUserId(request.getUserId()));
        
        try {
            Notification notification = notificationService.sendOrderCancellation(
                request.getOrderId(), request.getUserId()
            );
            telemetryClient.finishTrace("send_order_cancellation", 200, null);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            telemetryClient.finishTrace("send_order_cancellation", 500, e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/payment-confirmation")
    public ResponseEntity<Notification> sendPaymentConfirmation(@RequestBody PaymentConfirmationRequest request) {
        telemetryClient.startTrace("send_payment_confirmation", "POST", "/api/notifications/payment-confirmation", toUserId(request.getUserId()));
        
        try {
            Notification notification = notificationService.sendPaymentConfirmation(
                request.getPaymentId(), request.getUserId(), request.getOrderId()
            );
            telemetryClient.finishTrace("send_payment_confirmation", 200, null);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            telemetryClient.finishTrace("send_payment_confirmation", 500, e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/payment-failure")
    public ResponseEntity<Notification> sendPaymentFailure(@RequestBody PaymentFailureRequest request) {
        telemetryClient.startTrace("send_payment_failure", "POST", "/api/notifications/payment-failure", toUserId(request.getUserId()));
        
        try {
            Notification notification = notificationService.sendPaymentFailure(
                request.getPaymentId(), request.getUserId(), request.getOrderId()
            );
            telemetryClient.finishTrace("send_payment_failure", 200, null);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            telemetryClient.finishTrace("send_payment_failure", 500, e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/refund-confirmation")
    public ResponseEntity<Notification> sendRefundConfirmation(@RequestBody RefundConfirmationRequest request) {
        telemetryClient.startTrace("send_refund_confirmation", "POST", "/api/notifications/refund-confirmation", toUserId(request.getUserId()));
        
        try {
            Notification notification = notificationService.sendRefundConfirmation(
                request.getPaymentId(), request.getUserId(), request.getOrderId()
            );
            telemetryClient.finishTrace("send_refund_confirmation", 200, null);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            telemetryClient.finishTrace("send_refund_confirmation", 500, e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        telemetryClient.startTrace("get_notification_by_id", "GET", "/api/notifications/" + id, null);
        
        try {
            Notification notification = notificationService.getNotificationById(id);
            telemetryClient.finishTrace("get_notification_by_id", 200, null);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            telemetryClient.finishTrace("get_notification_by_id", 404, e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserId(@PathVariable Long userId) {
        telemetryClient.startTrace("get_notifications_by_user", "GET", "/api/notifications/user/" + userId, toUserId(userId));
        
        try {
            List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
            telemetryClient.finishTrace("get_notifications_by_user", 200, null);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            telemetryClient.finishTrace("get_notifications_by_user", 500, e.getMessage());
            throw e;
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        telemetryClient.startTrace("get_all_notifications", "GET", "/api/notifications", null);
        
        try {
            List<Notification> notifications = notificationService.getAllNotifications();
            telemetryClient.finishTrace("get_all_notifications", 200, null);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            telemetryClient.finishTrace("get_all_notifications", 500, e.getMessage());
            throw e;
        }
    }
}