package unicam.filiera.service;

import unicam.filiera.dao.ProdottoTrasformatoDAO;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.FaseProduzione;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.ProdottoTrasformatoNotifier;
import unicam.filiera.util.ValidatoreProdottoTrasformato;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ProdottoTrasformatoServiceImpl implements ProdottoTrasformatoService {

    private final ProdottoTrasformatoDAO dao;
    private final ProdottoTrasformatoNotifier notifier;

    public ProdottoTrasformatoServiceImpl(ProdottoTrasformatoDAO dao) {
        this.dao = dao;
        this.notifier = ProdottoTrasformatoNotifier.getInstance();
    }

    public ProdottoTrasformatoServiceImpl() {
        this(unicam.filiera.dao.JdbcProdottoTrasformatoDAO.getInstance());
    }

    @Override
    public void creaProdottoTrasformato(ProdottoTrasformatoDto dto, String creatore) {
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

        // Mapping delle fasi
        List<FaseProduzione> fasi = dto.getFasiProduzione().stream()
                .map(FaseProduzione::fromDto)
                .collect(Collectors.toList());

        // Validazione completa
        ValidatoreProdottoTrasformato.valida(dto.getNome(), dto.getDescrizione(), dto.getIndirizzo(), quantita, prezzo, fasi);
        ValidatoreProdottoTrasformato.validaFileCaricati(dto.getCertificati().size(), dto.getFoto().size());

        // Mappatura DTO -> Model
        ProdottoTrasformato prodotto = new ProdottoTrasformato.Builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .quantita(quantita)
                .prezzo(prezzo)
                .indirizzo(dto.getIndirizzo())
                .certificati(dto.getCertificati().stream().map(File::getName).toList())
                .foto(dto.getFoto().stream().map(File::getName).toList())
                .creatoDa(creatore)
                .stato(StatoProdotto.IN_ATTESA)
                .fasiProduzione(fasi)
                .build();

        // Salvataggio + notifiche
        if (!dao.save(prodotto, dto.getCertificati(), dto.getFoto())) {
            throw new RuntimeException("Errore durante il salvataggio del prodotto trasformato e dei file");
        }
        notifier.notificaTutti(prodotto, "NUOVO_PRODOTTO_TRASFORMATO");
    }

    @Override
    public void aggiornaProdottoTrasformato(String nomeOriginale, ProdottoTrasformatoDto dto, String creatore) {
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

        // Controlla esistenza e stato RIFIUTATO
        ProdottoTrasformato existing = dao.findByNomeAndCreatore(nomeOriginale, creatore);
        ValidatoreProdottoTrasformato.validaModifica(existing);

        // Mapping fasi
        List<FaseProduzione> fasi = dto.getFasiProduzione().stream()
                .map(FaseProduzione::fromDto)
                .collect(Collectors.toList());

        // Validazione aggiornata
        ValidatoreProdottoTrasformato.valida(dto.getNome(), dto.getDescrizione(), dto.getIndirizzo(), quantita, prezzo, fasi);
        ValidatoreProdottoTrasformato.validaFileCaricati(dto.getCertificati().size(), dto.getFoto().size());

        // Nuovo oggetto aggiornato
        ProdottoTrasformato updated = new ProdottoTrasformato.Builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .quantita(quantita)
                .prezzo(prezzo)
                .indirizzo(dto.getIndirizzo())
                .certificati(dto.getCertificati().stream().map(File::getName).toList())
                .foto(dto.getFoto().stream().map(File::getName).toList())
                .creatoDa(creatore)
                .stato(StatoProdotto.IN_ATTESA)
                .commento(null)
                .fasiProduzione(fasi)
                .build();

        boolean ok = dao.update(nomeOriginale, creatore, updated, dto.getCertificati(), dto.getFoto());
        if (!ok) throw new RuntimeException("Errore durante l'aggiornamento del prodotto trasformato");

        notifier.notificaTutti(updated, "NUOVO_PRODOTTO_TRASFORMATO");
    }

    @Override
    public List<ProdottoTrasformato> getProdottiTrasformatiCreatiDa(String creatore) {
        return dao.findByCreatore(creatore);
    }

    @Override
    public List<ProdottoTrasformato> getProdottiTrasformatiByStato(StatoProdotto stato) {
        return dao.findByStato(stato);
    }

    @Override
    public void eliminaProdottoTrasformato(String nome, String creatore) {
        ProdottoTrasformato p = dao.findByNomeAndCreatore(nome, creatore);
        ValidatoreProdottoTrasformato.validaEliminazione(p);

        boolean ok = dao.deleteByNomeAndCreatore(nome, creatore);
        if (!ok) {
            throw new RuntimeException("Errore durante l'eliminazione di \"" + nome + "\"");
        }
        notifier.notificaTutti(p, "ELIMINATO_PRODOTTO_TRASFORMATO");
    }

    /**
     * Notifica l’esito approvazione/rifiuto dal Curatore (chiamata opzionale, tipicamente dalla Controller del curatore).
     */
    public void notificaApprovazioneRifiuto(ProdottoTrasformato prodotto, boolean approvato) {
        notifier.notificaTutti(prodotto, approvato ? "APPROVATO" : "RIFIUTATO");
    }
}
