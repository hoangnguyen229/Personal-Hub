package hoangnguyen.dev.personal_hub_backend.enums;

import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTypeEnum {
    FOLLOW("FOLLOW"),
    COMMENT("COMMENT"),
    LIKE("LIKE"),
    POST("POST"),
    ;

    private final String value;

    public static NotificationTypeEnum fromValue(String value) {
        if (value == null) {
            throw new ApiException(ErrorCodeEnum.INVALID_NOTIFICATION_TYPE);
        }
        for (NotificationTypeEnum type : NotificationTypeEnum.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new ApiException(ErrorCodeEnum.INVALID_NOTIFICATION_TYPE);
    }
}
