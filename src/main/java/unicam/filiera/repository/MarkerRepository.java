package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.MarkerEntity;

import java.util.Optional;

@Repository
public interface MarkerRepository extends JpaRepository<MarkerEntity, Long> {
    Optional<MarkerEntity> findByLabel(String label);
}

