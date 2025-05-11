package unicam.filiera.util;

public class ValidatoreProdotto {

    /**
     * Valida i dati essenziali del prodotto.
     *
     * @throws IllegalArgumentException se un campo non è valido.
     */
    public static void valida(String nome,
                              String descrizione,
                              String indirizzo,
                              int quantita,
                              double prezzo) {

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
    }

    /* ------------------------------------------------------------------ */

    /**
     * Valida la presenza di almeno un certificato e di almeno una foto.
     */
    public static void validaFileCaricati(int numCertificati, int numFoto) {
        if (numCertificati < 1)
            throw new IllegalArgumentException("⚠ Devi selezionare almeno un certificato!");

        if (numFoto < 1)
            throw new IllegalArgumentException("⚠ Devi selezionare almeno una foto!");
    }
}
