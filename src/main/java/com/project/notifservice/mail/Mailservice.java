package com.project.notifservice.mail;

import com.project.notifservice.model.event.PaymentEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Mailservice {

    private final JavaMailSender mailSender;

    /**
     * Send payment / virement validation email
     */
    public void sendPaymentValidationEmail(PaymentEvent event) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; background-color:#f5f7fb; padding:20px;">
                <div style="max-width:600px; margin:auto; background:#ffffff;
                            border-radius:10px; padding:30px;
                            box-shadow:0 2px 10px rgba(0,0,0,0.1);">

                    <h2 style="color:#1976d2; text-align:center;">
                        ✔ Validation de votre virement
                    </h2>

                    <p>Bonjour <strong>%s</strong>,</p>

                    <p>Nous vous confirmons que votre opération bancaire a été traitée avec succès.</p>

                    <div style="background:#f0f4f8; padding:15px; border-radius:5px;">
                        <p><strong>Montant :</strong> %s</p>
                       
                      
                       
                      
                    </div>

                    <p style="margin-top:20px;">
                        <strong>Description :</strong><br/>
                        %s
                    </p>

                    <hr style="margin:30px 0;"/>

                    <p style="font-size:13px; color:#777;">
                        Si vous n’êtes pas à l’origine de cette opération, veuillez contacter
                        immédiatement notre support.
                    </p>

                    <p style="font-size:12px; color:#aaa; text-align:center;">
                        © 2024 - Bankino | Message automatique
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                event.getClientName(),
                event.getAmount(),


                event.getDescription()
        );

        helper.setTo(event.getClientEmail());
        helper.setSubject("Validation de votre virement bancaire");
        helper.setText(html, true);

        mailSender.send(message);
        System.out.println("Payment validation email sent to " + event.getClientEmail());
    }
}

