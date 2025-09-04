package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdottoRepository extends JpaRepository<ProdottoEntity, Long> {

    List<ProdottoEntity> findByCreatoDa(String creatore);

    List<ProdottoEntity> findByStato(StatoProdotto stato);

    Optional<ProdottoEntity> findByNomeAndCreatoDa(String nome, String creatore);

    boolean existsByNomeAndCreatoDa(String nome, String creatore);

    @Query("SELECT p.indirizzo FROM ProdottoEntity p WHERE LOWER(TRIM(p.nome)) = LOWER(TRIM(:nome))")
    Optional<String> findIndirizzoByNome(@Param("nome") String nome);

    // prodotti approvati di un produttore
    List<ProdottoEntity> findByStatoAndCreatoDa(StatoProdotto stato, String creatore);
}
