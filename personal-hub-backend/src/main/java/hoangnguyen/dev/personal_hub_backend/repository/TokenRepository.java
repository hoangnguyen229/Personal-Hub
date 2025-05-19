package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query(value = """
        select t from Token t inner join User u\s
        on t.user.userID = u.userID\s
        where u.userID = :userID and (t.expired = false or t.revoked = false)\s
      """)
    List<Token> findAllValidTokenByUser(Long userID);

    Optional<Token> findByToken(String jwt);
}
