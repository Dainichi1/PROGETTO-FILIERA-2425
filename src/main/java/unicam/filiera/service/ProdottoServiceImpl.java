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
        // 1. Parsing dei dati testuali
        int quantita;
        double prezzo;

        try {
            quantita = Integer.parseInt(dto.getQuantitaTxt());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("⚠ Quantità non valida (deve essere un intero positivo)");
        }

        try {
            prezzo = Double.parseDouble(dto.getPrezzoTxt());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("⚠ Prezzo non valido (deve essere un numero)");
        }

        // 2. Validazione dominio
        ValidatoreProdotto.valida(dto.getNome(), dto.getDescrizione(), dto.getIndirizzo(), quantita, prezzo);
        ValidatoreProdotto.validaFileCaricati(dto.getCertificati().size(), dto.getFoto().size());

        // 3. Mapping DTO → Domain
        Prodotto prodotto = new Prodotto.Builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .quantita(quantita)
                .prezzo(prezzo)
                .indirizzo(dto.getIndirizzo())
                .certificati(dto.getCertificati().stream().map(File::getName).toList())
                .foto(dto.getFoto().stream().map(File::getName).toList())
                .creatoDa(creatore)
                .stato(StatoProdotto.IN_ATTESA)
                .build();

        // 4. Salvataggio
        if (!dao.save(prodotto, dto.getCertificati(), dto.getFoto())) {
            throw new RuntimeException("Errore durante il salvataggio del prodotto e dei file");
        }


        // 5. Notifica observer
        notifier.notificaTutti(prodotto, "NUOVO_PRODOTTO");
    }


    @Override
    public List<Prodotto> getProdottiCreatiDa(String creatore) {
        return dao.findByCreatore(creatore);
    }

    @Override
    public List<Prodotto> getProdottiByStato(StatoProdotto stato) {
        return dao.findByStato(stato);
    }

    @Override
    public void eliminaProdotto(String nome, String creatore) {
        Prodotto p = dao.findByNomeAndCreatore(nome, creatore);

        // delega al validatore
        ValidatoreProdotto.validaEliminazione(p);

        boolean ok = dao.deleteByNomeAndCreatore(nome, creatore);
        if (!ok) {
            throw new RuntimeException("Errore durante l'eliminazione di \"" + nome + "\"");
        }

        notifier.notificaTutti(p, "ELIMINATO_PRODOTTO");
    }

}
