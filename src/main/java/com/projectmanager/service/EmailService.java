package com.projectmanager.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token, String baseUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Email Verification - Project Manager");
        String verificationLink = baseUrl + "/api/auth/verify?token=" + token;
        String htmlContent = "<p>Please verify your email by clicking on the following link:</p>" +
                "<p><a href='" + verificationLink + "'>Verify Email</a></p>" +
                "<p>If you did not register for Project Manager, please ignore this email.</p>";
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendTaskAssignmentEmail(String to, String taskTitle, String projectName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Task Assigned - Project Manager");
        String htmlContent = "<p>You have been assigned a new task:</p>" +
                "<p><strong>Task:</strong> " + taskTitle + "</p>" +
                "<p><strong>Project:</strong> " + projectName + "</p>" +
                "<p>Please log in to Project Manager to view details.</p>";
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token, String baseUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Password Reset - Project Manager");
        String resetLink = baseUrl + "/api/auth/reset-password?token=" + token;
        String htmlContent = "<p>You requested a password reset for your Project Manager account:</p>" +
                "<p><a href='" + resetLink + "'>Reset Password</a></p>" +
                "<p>If you did not request this, please ignore this email.</p>";
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

}
