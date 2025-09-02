package unicam.filiera.service;

import org.springframework.stereotype.Service;
import unicam.filiera.model.*;
import unicam.filiera.repository.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketplaceService {

    private final ProdottoRepository prodottoRepository;


    public MarketplaceService(ProdottoRepository prodottoRepository) {
        this.prodottoRepository = prodottoRepository;

    }

    public List<Object> ottieniElementiMarketplace() {
        return new ArrayList<>(prodottoRepository.findByStato(StatoProdotto.APPROVATO));
    }
}
