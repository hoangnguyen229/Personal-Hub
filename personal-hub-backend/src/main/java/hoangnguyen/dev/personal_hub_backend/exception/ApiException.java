package hoangnguyen.dev.personal_hub_backend.exception;

import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCodeEnum errorCodeEnum;

    public ApiException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.errorCodeEnum = errorCodeEnum;
    }

    public int getCode() {
        return errorCodeEnum.getCode();
    }

    public HttpStatus getHttpStatus() {
        return errorCodeEnum.getHttpStatus();
    }
}
