package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacchettoRepository extends JpaRepository<PacchettoEntity, Long> {

    // Trova pacchetti creati da un certo distributore
    List<PacchettoEntity> findByCreatoDa(String creatore);

    // Trova pacchetti con un certo stato
    List<PacchettoEntity> findByStato(StatoProdotto stato);

    // Trova un pacchetto specifico
    Optional<PacchettoEntity> findByNomeAndCreatoDa(String nome, String creatore);

    // Controlla se un pacchetto esiste
    boolean existsByNomeAndCreatoDa(String nome, String creatore);

    // Trova solo l’indirizzo del pacchetto (case insensitive, trim)
    @Query("SELECT p.indirizzo FROM PacchettoEntity p WHERE LOWER(TRIM(p.nome)) = LOWER(TRIM(:nome))")
    Optional<String> findIndirizzoByNome(@Param("nome") String nome);

    // aggiorna quantità disponibile dopo un acquisto
    @Modifying
    @Transactional
    @Query("UPDATE PacchettoEntity p SET p.quantita = p.quantita - :qta WHERE p.id = :id AND p.quantita >= :qta")
    int decrementaQuantita(@Param("id") Long id, @Param("qta") int qta);
}


