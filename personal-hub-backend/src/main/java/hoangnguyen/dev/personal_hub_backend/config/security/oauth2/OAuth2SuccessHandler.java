package hoangnguyen.dev.personal_hub_backend.config.security.oauth2;


import hoangnguyen.dev.personal_hub_backend.enums.AuthTypeEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.Normalizer;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${frontend.callback.url}")
    private String frontendCallbackUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException{
        String email;
        String username;
        AuthTypeEnum provider;

        if(authentication.getPrincipal() instanceof OidcUser oidcUser){
            email = oidcUser.getEmail();
            username = sanitizeUsername(oidcUser.getGivenName());
            provider = AuthTypeEnum.GOOGLE;
        }else{
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauth2Token.getPrincipal();

            email = extractEmail(oAuth2User);
            username = oAuth2User.getAttribute("login");
            provider = AuthTypeEnum.GITHUB;
        }

        String redirectUri = UriComponentsBuilder
                .fromUriString(frontendCallbackUrl)
                .queryParam("email", email)
                .queryParam("username", username)
                .queryParam("provider", provider)
                .build()
                .encode()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

    private String extractEmail(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        if (email == null || email.isEmpty()) {
            String username = oauth2User.getAttribute("login");
            return username + "@github.user";
        }
        return email;
    }

    private String sanitizeUsername(String input) {
        if (input == null || input.isEmpty()) {
            return "user_" + UUID.randomUUID().toString().substring(0, 8);
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        String sanitized = normalized
                .replaceAll("[\\s-]+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toLowerCase();

        if (sanitized.isEmpty()) {
            sanitized = "user_" + UUID.randomUUID().toString().substring(0, 8);
        }

        return sanitized.length() > 50 ? sanitized.substring(0, 50) : sanitized;
    }
}
