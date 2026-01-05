package com.project.notifservice.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        // Initialize the Twilio client once at startup
        Twilio.init(accountSid, authToken);
        log.info("Twilio initialized with Account SID: {}", accountSid);
    }

    public void sendSms(String toPhoneNumber, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber), // To
                    new PhoneNumber(fromPhoneNumber), // From
                    messageBody                       // Body
            ).create();

            log.info("SMS sent to {} | SID: {}", toPhoneNumber, message.getSid());
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", toPhoneNumber, e.getMessage());
        }
    }
}
