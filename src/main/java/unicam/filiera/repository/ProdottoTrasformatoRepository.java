package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.ProdottoTrasformatoEntity;
import unicam.filiera.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdottoTrasformatoRepository extends JpaRepository<ProdottoTrasformatoEntity, Long> {

    // Trova prodotti trasformati creati da un certo trasformatore
    List<ProdottoTrasformatoEntity> findByCreatoDa(String creatore);

    // Trova prodotti trasformati con un certo stato
    List<ProdottoTrasformatoEntity> findByStato(StatoProdotto stato);

    // Trova un prodotto trasformato specifico
    Optional<ProdottoTrasformatoEntity> findByNomeAndCreatoDa(String nome, String creatore);

    // Controlla se un prodotto trasformato esiste
    boolean existsByNomeAndCreatoDa(String nome, String creatore);

    // Trova solo l’indirizzo (case insensitive, trim)
    @Query("SELECT p.indirizzo FROM ProdottoTrasformatoEntity p WHERE LOWER(TRIM(p.nome)) = LOWER(TRIM(:nome))")
    Optional<String> findIndirizzoByNome(@Param("nome") String nome);
}
