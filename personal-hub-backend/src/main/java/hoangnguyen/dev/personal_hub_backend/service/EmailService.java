package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.entity.User;

/**
 * Service interface for email-related operations
 * Handles sending various types of emails to users
 */
public interface EmailService {
    /**
     * Send an email containing a one-time password to a user
     * Used during password reset or verification processes
     * 
     * @param user the user to send the email to
     * @param otp the one-time password to include in the email
     */
    void sendOTPEmail(User user, String otp);
}