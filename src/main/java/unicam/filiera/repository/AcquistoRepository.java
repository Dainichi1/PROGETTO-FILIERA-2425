package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.AcquistoEntity;

import java.util.List;

@Repository
public interface AcquistoRepository extends JpaRepository<AcquistoEntity, Long> {

    // recupera tutti gli acquisti di un utente ordinati per data decrescente
    List<AcquistoEntity> findByUsernameAcquirenteOrderByDataOraDesc(String username);
}
