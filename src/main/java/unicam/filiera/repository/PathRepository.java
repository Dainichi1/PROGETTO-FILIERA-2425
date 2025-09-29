package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.PathEntity;

@Repository
public interface PathRepository extends JpaRepository<PathEntity, Long> {
}
