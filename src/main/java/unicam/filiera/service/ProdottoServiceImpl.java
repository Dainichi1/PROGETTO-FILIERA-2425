package unicam.filiera.service;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;
import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.util.ValidatoreProdotto;

import java.util.List;

/**
 * Service per la logica di Prodotto con parsing controllato.
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
        // parsing controllato di quantità e prezzo
        int quantita;
        try {
            quantita = Integer.parseInt(dto.getQuantitaTxt());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("⚠ Quantità non valida (deve essere un intero)");
        }

        double prezzo;
        try {
            prezzo = Double.parseDouble(dto.getPrezzoTxt());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("⚠ Prezzo non valido (deve essere un numero)");
        }

        // validazioni di dominio
        ValidatoreProdotto.valida(
                dto.getNome(),
                dto.getDescrizione(),
                dto.getIndirizzo(),
                quantita,
                prezzo
        );
        ValidatoreProdotto.validaFileCaricati(
                dto.getCertificati().size(),
                dto.getFoto().size()
        );

        // costruzione del dominio in stato IN_ATTESA
        Prodotto p = new Prodotto.Builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .quantita(quantita)
                .prezzo(prezzo)
                .indirizzo(dto.getIndirizzo())
                .creatoDa(creatore)
                .stato(StatoProdotto.IN_ATTESA)
                .build();

        // persistenza + upload files
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
