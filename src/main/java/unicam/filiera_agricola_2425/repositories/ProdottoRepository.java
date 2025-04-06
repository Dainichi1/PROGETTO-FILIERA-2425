package unicam.filiera_agricola_2425.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.filiera_agricola_2425.models.Prodotto;
import unicam.filiera_agricola_2425.models.Produttore;

import java.util.List;

public interface ProdottoRepository extends JpaRepository<Prodotto, Long> {

    List<Prodotto> findByProduttore(Produttore produttore);
    List<Prodotto> findByStato(Prodotto.StatoProdotto stato);

}
