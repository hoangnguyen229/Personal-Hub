package hoangnguyen.dev.personal_hub_backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthTypeEnum {
    LOCAL("LOCAL"),
    GOOGLE("GOOGLE"),
    GITHUB("GITHUB");

    private final String value;
}
