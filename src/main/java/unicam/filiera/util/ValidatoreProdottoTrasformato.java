package unicam.filiera.util;

import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.FaseProduzione;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

public class ValidatoreProdottoTrasformato {

    /**
     * Valida i dati essenziali del prodotto trasformato.
     *
     * @throws IllegalArgumentException se un campo non è valido.
     */
    public static void valida(String nome,
                              String descrizione,
                              String indirizzo,
                              int quantita,
                              double prezzo,
                              List<FaseProduzione> fasi) {

        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("⚠ Nome mancante o vuoto");

        if (descrizione == null || descrizione.isBlank())
            throw new IllegalArgumentException("⚠ Descrizione mancante o vuota");

        if (indirizzo == null || indirizzo.isBlank())
            throw new IllegalArgumentException("⚠ Indirizzo mancante o vuoto");

        if (quantita <= 0)
            throw new IllegalArgumentException("⚠ La quantità deve essere maggiore di zero");

        if (prezzo <= 0)
            throw new IllegalArgumentException("⚠ Il prezzo deve essere maggiore di zero");

        if (fasi == null || fasi.isEmpty())
            throw new IllegalArgumentException("⚠ Devi inserire almeno una fase di produzione!");

        // VALIDAZIONE DI OGNI FASE
        int i = 1;
        for (FaseProduzione fase : fasi) {
            if (fase == null)
                throw new IllegalArgumentException("⚠ La fase #" + i + " è nulla");
            if (fase.getDescrizioneFase() == null || fase.getDescrizioneFase().isBlank())
                throw new IllegalArgumentException("⚠ La descrizione della fase #" + i + " è mancante o vuota");
            if (fase.getProduttoreUsername() == null || fase.getProduttoreUsername().isBlank())
                throw new IllegalArgumentException("⚠ Il produttore della fase #" + i + " è mancante o vuoto");
            if (fase.getProdottoOrigine() == null || fase.getProdottoOrigine().isBlank())
                throw new IllegalArgumentException("⚠ Il prodotto di origine della fase #" + i + " è mancante o vuoto");
            i++;
        }
    }

    public static void validaFileCaricati(int numCertificati, int numFoto) {
        if (numCertificati < 1)
            throw new IllegalArgumentException("⚠ Devi selezionare almeno un certificato!");
        if (numFoto < 1)
            throw new IllegalArgumentException("⚠ Devi selezionare almeno una foto!");
    }

    public static void validaEliminazione(ProdottoTrasformato p) {
        if (p == null)
            throw new IllegalArgumentException("Prodotto trasformato non trovato");
        if (p.getStato() == StatoProdotto.APPROVATO)
            throw new IllegalStateException("Non puoi eliminare un prodotto trasformato già approvato");
    }

    public static void validaModifica(ProdottoTrasformato p) {
        if (p == null)
            throw new IllegalArgumentException("Prodotto trasformato non trovato per la modifica");
        if (p.getStato() != StatoProdotto.RIFIUTATO)
            throw new IllegalStateException("Puoi modificare solo prodotti trasformati con stato RIFIUTATO");
    }
}
