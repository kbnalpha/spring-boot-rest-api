package com.ehspro.service;

import com.ehspro.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ActivationEmailService {
    private final JavaMailSender sender;
    private final String from;
    public ActivationEmailService(JavaMailSender sender,@Value("${ehs.mail.from}") String from) {this.sender=sender;this.from=from;}
    public void send(String email,String temporaryPassword,java.time.LocalDateTime expiresAt) {
        SimpleMailMessage message=new SimpleMailMessage();message.setFrom(from);message.setTo(email);
        message.setSubject("EHS account activation - set your password");
        message.setText("Your EHS system account has been enabled.\n\nUsername: "+email+
            "\nTemporary password: "+temporaryPassword+"\nExpires at (UTC): "+expiresAt+
            "\n\nOn your first login you must replace this temporary password before accessing the system.\n"+
            "If the password expires, contact your Admin to resend your activation email.\n");
        try {sender.send(message);}
        catch(MailException ex) {throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"Activation email could not be sent. Account changes were not saved; check SMTP configuration and retry.");}
    }
}
