package unicam.filiera.util;

import java.util.List;

import unicam.filiera.model.Prodotto;

public class ValidatorePacchetto {

    /**
     * Valida i dati essenziali di un pacchetto.
     */
    public static void valida(String nome,
                              String descrizione,
                              String indirizzo,      // <-- nuovo
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
            throw new IllegalArgumentException("⚠ Devi selezionare almeno due prodotti per creare un pacchetto!");
    }


    /**
     * Valida se almeno un file è presente.
     */
    public static void validaFileCaricati(int numCertificati, int numFoto) {
        if (numCertificati < 1)
            throw new IllegalArgumentException("⚠ Devi selezionare almeno un certificato per il pacchetto!");

        if (numFoto < 1)
            throw new IllegalArgumentException("⚠ Devi selezionare almeno una foto per il pacchetto!");
    }
}
