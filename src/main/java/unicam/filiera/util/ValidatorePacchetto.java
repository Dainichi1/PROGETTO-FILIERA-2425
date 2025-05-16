package unicam.filiera.util;

import java.util.List;

import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.Pacchetto;

/**
 * Validatore per la logica di dominio dei Pacchetti.
 */
public class ValidatorePacchetto {

    /**
     * Valida i dati essenziali di un pacchetto.
     */
    public static void valida(String nome,
                              String descrizione,
                              String indirizzo,
                              double prezzoTotale,
                              List<Prodotto> prodotti) {

        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("⚠ Nome pacchetto mancante o vuoto");

        if (descrizione == null || descrizione.isBlank())
            throw new IllegalArgumentException("⚠ Descrizione pacchetto mancante o vuota");

        if (indirizzo == null || indirizzo.isBlank())
            throw new IllegalArgumentException("⚠ Indirizzo mancante o vuoto");

        if (prezzoTotale <= 0)
            throw new IllegalArgumentException("⚠ Il prezzo totale deve essere maggiore di zero");

        if (prodotti == null || prodotti.size() < 2)
            throw new IllegalArgumentException(
                    "⚠ Devi selezionare almeno due prodotti per creare un pacchetto!");
    }

    /**
     * Valida la presenza di almeno un certificato e una foto.
     */
    public static void validaFileCaricati(int numCertificati, int numFoto) {
        if (numCertificati < 1)
            throw new IllegalArgumentException(
                    "⚠ Devi selezionare almeno un certificato per il pacchetto!");

        if (numFoto < 1)
            throw new IllegalArgumentException(
                    "⚠ Devi selezionare almeno una foto per il pacchetto!");
    }

    /**
     * Valida se un pacchetto può essere eliminato.
     *
     * @throws IllegalArgumentException se non trovato
     * @throws IllegalStateException    se già approvato
     */
    public static void validaEliminazione(Pacchetto p) {
        if (p == null)
            throw new IllegalArgumentException("Pacchetto non trovato");

        if (p.getStato() == StatoProdotto.APPROVATO)
            throw new IllegalStateException("Non puoi eliminare un pacchetto già approvato");
    }

    /**
     * Valida se un pacchetto può essere modificato (deve essere in stato RIFIUTATO).
     *
     * @throws IllegalArgumentException se non trovato
     * @throws IllegalStateException    se stato diverso da RIFIUTATO
     */
    public static void validaModifica(Pacchetto p) {
        if (p == null) {
            throw new IllegalArgumentException("Pacchetto non trovato per la modifica");
        }
        if (p.getStato() != StatoProdotto.RIFIUTATO) {
            throw new IllegalStateException(
                    "Puoi modificare solo pacchetti con stato RIFIUTATO");
        }
    }
}
