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

    // Trova prodotti creati da un certo utente
    List<ProdottoEntity> findByCreatoDa(String creatore);

    // Trova prodotti con un certo stato
    List<ProdottoEntity> findByStato(StatoProdotto stato);

    // Trova un prodotto specifico
    Optional<ProdottoEntity> findByNomeAndCreatoDa(String nome, String creatore);

    // Controlla se un prodotto esiste
    boolean existsByNomeAndCreatoDa(String nome, String creatore);

    // Trova solo l’indirizzo (case insensitive, trim)
    @Query("SELECT p.indirizzo FROM ProdottoEntity p WHERE LOWER(TRIM(p.nome)) = LOWER(TRIM(:nome))")
    Optional<String> findIndirizzoByNome(@Param("nome") String nome);
}
