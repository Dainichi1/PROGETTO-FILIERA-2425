package unicam.filiera.service;

import org.springframework.stereotype.Service;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.repository.*;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.ProdottoTrasformatoEntity;
import unicam.filiera.entity.VisitaInvitoEntity;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketplaceService {

    private final ProdottoRepository prodottoRepository;
    private final PacchettoRepository pacchettoRepository;
    private final ProdottoTrasformatoRepository prodottoTrasformatoRepository;
    private final VisitaInvitoRepository visitaInvitoRepository;
    private final FieraRepository fieraRepository;

    public MarketplaceService(ProdottoRepository prodottoRepository,
                              PacchettoRepository pacchettoRepository,
                              ProdottoTrasformatoRepository prodottoTrasformatoRepository,
                              VisitaInvitoRepository visitaInvitoRepository, FieraRepository fieraRepository) {
        this.prodottoRepository = prodottoRepository;
        this.pacchettoRepository = pacchettoRepository;
        this.prodottoTrasformatoRepository = prodottoTrasformatoRepository;
        this.visitaInvitoRepository = visitaInvitoRepository;
        this.fieraRepository = fieraRepository;
    }

    public List<Object> ottieniElementiMarketplace() {
        List<Object> elementi = new ArrayList<>();
        elementi.addAll(prodottoRepository.findByStato(StatoProdotto.APPROVATO));
        elementi.addAll(pacchettoRepository.findByStato(StatoProdotto.APPROVATO));
        elementi.addAll(prodottoTrasformatoRepository.findByStato(StatoProdotto.APPROVATO));
        elementi.addAll(visitaInvitoRepository.findByStato(StatoEvento.PUBBLICATA));
        elementi.addAll(fieraRepository.findByStato(StatoEvento.PUBBLICATA));
        return elementi;
    }
}
