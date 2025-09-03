package unicam.filiera.service;

import org.springframework.stereotype.Service;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.repository.PacchettoRepository;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.entity.ProdottoEntity;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketplaceService {

    private final ProdottoRepository prodottoRepository;
    private final PacchettoRepository pacchettoRepository;

    public MarketplaceService(ProdottoRepository prodottoRepository,
                              PacchettoRepository pacchettoRepository) {
        this.prodottoRepository = prodottoRepository;
        this.pacchettoRepository = pacchettoRepository;
    }

    /**
     * Ritorna tutti gli elementi (prodotti + pacchetti) approvati
     */
    public List<Object> ottieniElementiMarketplace() {
        List<Object> elementi = new ArrayList<>();
        elementi.addAll(prodottoRepository.findByStato(StatoProdotto.APPROVATO));
        elementi.addAll(pacchettoRepository.findByStato(StatoProdotto.APPROVATO));
        return elementi;
    }
}
