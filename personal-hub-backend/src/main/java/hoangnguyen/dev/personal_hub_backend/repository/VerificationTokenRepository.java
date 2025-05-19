package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.entity.VerificationToken;
import hoangnguyen.dev.personal_hub_backend.enums.TokenPurposeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByUserAndTokenAndTokenTypeAndIsVerifiedFalse(User user, String token, TokenPurposeEnum tokenType);
    Optional<VerificationToken> findByTokenAndTokenTypeAndIsVerifiedFalse(String token, TokenPurposeEnum tokenType);
}
