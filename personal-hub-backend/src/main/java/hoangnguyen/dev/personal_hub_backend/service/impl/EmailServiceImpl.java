package hoangnguyen.dev.personal_hub_backend.service.impl;


import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the EmailService interface
 * Handles sending emails using Thymeleaf templates and Spring Mail
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Personal Hub}")
    private String appName;

    /**
     * Sends an email with a one-time password for verification
     * Runs asynchronously to avoid blocking the main application flow
     * 
     * @param user the recipient user
     * @param otp the one-time password to include in the email
     * @throws ApiException if email sending fails
     */
    @Override
    @Async
    public void sendOTPEmail(User user, String otp) {
        try {
            String emailContent = buildOTPEmailContent(user, otp);

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(appName + " - Password Reset Verification Code");
            helper.setText(emailContent, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new ApiException(ErrorCodeEnum.EMAIL_SENDING_FAILED);
        }
    }

    /**
     * Builds the HTML content for OTP email using Thymeleaf template
     * 
     * @param user the recipient user
     * @param otp the one-time password to include in the email
     * @return the processed HTML content as a string
     */
    private String buildOTPEmailContent(User user, String otp) {
        Context context = new Context();
        Map<String, Object> variables = new HashMap<>();

        variables.put("username", user.getUsername());
        variables.put("otp", otp);
        variables.put("appName", appName);
        variables.put("validityMinutes", 5);

        context.setVariables(variables);

        return templateEngine.process("otp-email-template", context);
    }
}