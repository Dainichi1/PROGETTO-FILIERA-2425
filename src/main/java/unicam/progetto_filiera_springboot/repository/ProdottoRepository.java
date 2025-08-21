package unicam.progetto_filiera_springboot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unicam.progetto_filiera_springboot.domain.model.Prodotto;
import unicam.progetto_filiera_springboot.domain.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdottoRepository extends JpaRepository<Prodotto, Long> {

    boolean existsByNomeAndCreatoDa_Username(String nome, String username);

    Optional<Prodotto> findByNomeAndCreatoDa_Username(String nome, String username);

    Page<Prodotto> findByCreatoDa_Username(String username, Pageable pageable);

    Page<Prodotto> findByStato(StatoProdotto stato, Pageable pageable);

    List<Prodotto> findByCreatoDa_UsernameOrderByCreatedAtDesc(String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Prodotto p SET p.stato = :stato, p.commento = :commento WHERE p.id = :id")
    int updateStatoAndCommento(@Param("id") Long id,
                               @Param("stato") StatoProdotto stato,
                               @Param("commento") String commento);

    List<Prodotto> findByStato(StatoProdotto stato);
}
