package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.PostSocialEntity;

import java.util.List;

@Repository
public interface PostSocialRepository extends JpaRepository<PostSocialEntity, Long> {

    List<PostSocialEntity> findAllByOrderByCreatedAtDesc();

    List<PostSocialEntity> findByAutoreUsernameOrderByCreatedAtDesc(String username);
}
