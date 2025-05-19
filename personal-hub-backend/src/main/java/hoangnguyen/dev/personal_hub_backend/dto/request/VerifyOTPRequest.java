package hoangnguyen.dev.personal_hub_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOTPRequest {
    private String email;

    @NotBlank(message = "OTP cannot be blank")
    @Size(min = 6)
    private String otp;
}
