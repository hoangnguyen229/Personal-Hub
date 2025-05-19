package hoangnguyen.dev.personal_hub_backend.validator;

import hoangnguyen.dev.personal_hub_backend.dto.request.RegisterRequest;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;

import java.util.regex.Pattern;

public class RegisterValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    public static void validateRegisterRequest(RegisterRequest request) {
        validateEmail(request.getEmail());
        validatePassword(request.getPassword());
    }

    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(ErrorCodeEnum.INVALID_EMAIL_FORMAT);
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ApiException(ErrorCodeEnum.WEAK_PASSWORD);
        }
    }
}