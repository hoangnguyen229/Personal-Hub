package hoangnguyen.dev.personal_hub_backend.config.security.oauth2;

import hoangnguyen.dev.personal_hub_backend.enums.AuthTypeEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Value("${frontend.callback.url}")
    private String frontendCallbackUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,

            AuthenticationException exception
    ) throws IOException, ServletException {

        AuthTypeEnum provider = null;
        if (request.getRequestURI().contains("/login/oauth2/code/github")) {
            provider = AuthTypeEnum.GITHUB;
        } else if (request.getRequestURI().contains("/login/oauth2/code/google")) {
            provider = AuthTypeEnum.GOOGLE;
        }

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(frontendCallbackUrl)
                .queryParam("status", "failed")
                .queryParam("error", exception.getMessage());

        if (provider != null) {
            builder.queryParam("provider", provider);
        }

        String redirectUrl = builder.build().encode().toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
