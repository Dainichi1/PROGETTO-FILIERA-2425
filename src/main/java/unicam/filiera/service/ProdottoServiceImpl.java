// -------- ProdottoServiceImpl.java --------
package unicam.filiera.service;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;
import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.util.ValidatoreProdotto;

import java.io.File;
import java.util.List;

/**
 * Service per la logica di Prodotto.
 */
public class ProdottoServiceImpl implements ProdottoService {
    private final ProdottoDAO dao;
    private final ProdottoNotifier notifier;

    public ProdottoServiceImpl(ProdottoDAO dao) {
        this.dao = dao;
        this.notifier = ProdottoNotifier.getInstance();
    }

    @Override
    public void creaProdotto(ProdottoDto dto, String creatore) {
        // validazioni
        ValidatoreProdotto.valida(
                dto.getNome(), dto.getDescrizione(), dto.getIndirizzo(),
                Integer.parseInt(dto.getQuantitaTxt()),
                Double.parseDouble(dto.getPrezzoTxt())
        );
        ValidatoreProdotto.validaFileCaricati(
                dto.getCertificati().size(), dto.getFoto().size()
        );

        // costruzione dominio (senza file)
        Prodotto p = new Prodotto.Builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .quantita(Integer.parseInt(dto.getQuantitaTxt()))
                .prezzo(Double.parseDouble(dto.getPrezzoTxt()))
                .indirizzo(dto.getIndirizzo())
                .creatoDa(creatore)
                .stato(StatoProdotto.IN_ATTESA)
                .build();

        // persistenza + upload
        if (!dao.save(p, dto.getCertificati(), dto.getFoto())) {
            throw new RuntimeException("Errore durante il salvataggio del prodotto");
        }

        // notifica observer
        notifier.notificaTutti(p, "NUOVO_PRODOTTO");
    }

    @Override
    public List<Prodotto> getProdottiCreatiDa(String creatore) {
        return dao.findByCreatore(creatore);
    }

    @Override
    public List<Prodotto> getProdottiByStato(StatoProdotto stato) {
        return dao.findByStato(stato);
    }
}
