package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.AcquistoItemEntity;

import java.util.List;

@Repository
public interface AcquistoItemRepository extends JpaRepository<AcquistoItemEntity, Long> {

    // tutti gli item collegati a un acquisto
    List<AcquistoItemEntity> findByAcquistoId(Long acquistoId);
}
