package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.model.Notification;
import com.ecommerce.notificationservice.telemetry.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private TelemetryClient telemetryClient;
    
    @Value("${email.service.delay.ms:500}")
    private int emailDelayMs;
    
    @Value("${sms.service.delay.ms:300}")
    private int smsDelayMs;
    
    public void sendNotification(Notification notification) {
        long startTime = System.currentTimeMillis();
        int statusCode = 200;
        
        // Simulate email sending
        try {
            if (emailDelayMs > 0) {
                Thread.sleep(emailDelayMs); // Simulate email sending delay
            }
            
            // Simulate random email failures (5% chance)
            if (Math.random() < 0.05) {
                throw new RuntimeException("Email service unavailable");
            }
            
            // In a real implementation, this would integrate with an email service like:
            // - SendGrid
            // - Amazon SES
            // - Mailgun
            // - SMTP server
            
            System.out.println("📧 Email sent successfully:");
            System.out.println("  To: User ID " + notification.getUserId());
            System.out.println("  Subject: " + notification.getSubject());
            System.out.println("  Type: " + notification.getType());
            System.out.println("  Message: " + notification.getMessage().substring(0, Math.min(50, notification.getMessage().length())) + "...");
            
        } catch (InterruptedException e) {
            statusCode = 500;
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email sending interrupted");
        } catch (RuntimeException e) {
            statusCode = 500;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            telemetryClient.recordServiceCall("email-provider", "send_email", "POST", 
                "smtp://email.service", duration, statusCode);
        }
    }
    
    public void sendSMS(String phoneNumber, String message) {
        long startTime = System.currentTimeMillis();
        int statusCode = 200;
        
        // Simulate SMS sending
        try {
            if (smsDelayMs > 0) {
                Thread.sleep(smsDelayMs); // Simulate SMS sending delay
            }
            
            // Simulate random SMS failures (3% chance)
            if (Math.random() < 0.03) {
                throw new RuntimeException("SMS service unavailable");
            }
            
            System.out.println("📱 SMS sent successfully:");
            System.out.println("  To: " + phoneNumber);
            System.out.println("  Message: " + message);
            
        } catch (InterruptedException e) {
            statusCode = 500;
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS sending interrupted");
        } catch (RuntimeException e) {
            statusCode = 500;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            telemetryClient.recordServiceCall("sms-provider", "send_sms", "POST", 
                "https://sms.service/api/send", duration, statusCode);
        }
    }
}