package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.FieraEntity;
import unicam.filiera.model.StatoEvento;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieraRepository extends JpaRepository<FieraEntity, Long> {

    // tutte le fiere create da un utente (animatore)
    List<FieraEntity> findByCreatoDa(String creatore);

    // per evitare duplicati: fiera con stesso nome e creatore
    Optional<FieraEntity> findByNomeAndCreatoDa(String nome, String creatore);

    boolean existsByNomeAndCreatoDa(String nome, String creatore);

    // per recuperare indirizzo a partire dal nome della fiera
    @Query("SELECT f.indirizzo FROM FieraEntity f WHERE LOWER(TRIM(f.nome)) = LOWER(TRIM(:nome))")
    Optional<String> findIndirizzoByNome(@Param("nome") String nome);

    List<FieraEntity> findByStato(StatoEvento stato);
}
