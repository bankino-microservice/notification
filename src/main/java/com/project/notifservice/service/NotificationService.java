package com.project.notifservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.notifservice.mail.Mailservice;
import com.project.notifservice.model.event.PaymentEvent;
import com.project.notifservice.sms.SmsService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
@Autowired
    private final Mailservice mailService;
@Autowired
    private final SmsService smsService;
    /**
     * Consume payment events from Kafka
     * and send validation email to the client
     */
    @KafkaListener(
            topics = "payment-events",
            groupId = "notification-group"
    )
    public void consumePaymentEvent(PaymentEvent event) {
        try {
            // Send payment / virement validation email
            mailService.sendPaymentValidationEmail(event);



        } catch (MessagingException e) {

            e.printStackTrace();
        }
    }
    @KafkaListener(topics = "payment-events", groupId = "notif-group")
    public void handlePaymentEvent(PaymentEvent event) {


        // 1. Check Phone Number gracefully
        if (event.getClientPhoneNumber() == null || event.getClientPhoneNumber().isEmpty()) {
            // ⚠️ FIX: Log a warning instead of throwing RuntimeException

            return; // Exit the function safely so Kafka can move to the next message
        }

        // 2. If phone exists, send SMS
        try {
            String smsBody = """
            Bonjour %s,
            vous avez recu %s .
            
            """.formatted(
                    event.getClientName(),
                    event.getAmount()

            );

            smsService.sendSms(event.getClientPhoneNumber(), smsBody);

        } catch (Exception e) {
            // Catch SMS provider errors so they don't block the queue
            throw new RuntimeException("could not send payment event", e);
        }
    }
}
