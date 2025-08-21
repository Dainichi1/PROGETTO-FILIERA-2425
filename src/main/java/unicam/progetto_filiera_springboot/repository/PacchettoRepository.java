package unicam.progetto_filiera_springboot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unicam.progetto_filiera_springboot.domain.model.Pacchetto;
import unicam.progetto_filiera_springboot.domain.model.StatoPacchetto;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacchettoRepository extends JpaRepository<Pacchetto, Long> {

    boolean existsByNomeAndCreatoDa_Username(String nome, String username);

    Optional<Pacchetto> findByNomeAndCreatoDa_Username(String nome, String username);

    Page<Pacchetto> findByCreatoDa_Username(String username, Pageable pageable);

    Page<Pacchetto> findByStato(StatoPacchetto stato, Pageable pageable);

    List<Pacchetto> findByCreatoDa_UsernameOrderByCreatedAtDesc(String username);

    List<Pacchetto> findByStato(StatoPacchetto stato);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Pacchetto p SET p.stato = :stato, p.commento = :commento WHERE p.id = :id")
    int updateStatoAndCommento(@Param("id") Long id,
                               @Param("stato") StatoPacchetto stato,
                               @Param("commento") String commento);
}
