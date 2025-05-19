package hoangnguyen.dev.personal_hub_backend.service.impl;

import hoangnguyen.dev.personal_hub_backend.entity.Token;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.entity.VerificationToken;
import hoangnguyen.dev.personal_hub_backend.enums.TokenPurposeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.TokenTypeEnum;
import hoangnguyen.dev.personal_hub_backend.repository.TokenRepository;
import hoangnguyen.dev.personal_hub_backend.repository.VerificationTokenRepository;
import hoangnguyen.dev.personal_hub_backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUserID());
        if(validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    @Override
    public void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenTypeEnum(TokenTypeEnum.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    @Override
    public void saveVerificationToken(VerificationToken verificationToken) {
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public Optional<VerificationToken> findValidOTP(User user, String otp, TokenPurposeEnum tokenType) {
        return verificationTokenRepository.findByUserAndTokenAndTokenTypeAndIsVerifiedFalse(user, otp, tokenType)
                .filter(token -> token.getExpiryTime().isAfter(LocalDateTime.now()));
    }

    @Override
    public Optional<VerificationToken> findValidToken(String token, TokenPurposeEnum tokenType) {
        return verificationTokenRepository.findByTokenAndTokenTypeAndIsVerifiedFalse(token, tokenType)
                .filter(vtoken -> vtoken.getExpiryTime().isAfter(LocalDateTime.now()));
    }

    @Override
    public void updateVerificationToken(VerificationToken token) {
        verificationTokenRepository.save(token);
    }
}
