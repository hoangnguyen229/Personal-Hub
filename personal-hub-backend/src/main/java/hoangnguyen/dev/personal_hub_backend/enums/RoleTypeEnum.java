package hoangnguyen.dev.personal_hub_backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleTypeEnum {
    ROLE_ADMIN(1),
    ROLE_USER(2);

    private final int value;
}
