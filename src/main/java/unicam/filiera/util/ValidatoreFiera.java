package unicam.filiera.util;

import java.time.LocalDateTime;

public class ValidatoreFiera {

    /**
     * Controlla che la fine sia dopo l’inizio
     */
    public static void validaDate(LocalDateTime inizio, LocalDateTime fine) {
        if (inizio == null || fine == null || !fine.isAfter(inizio)) {
            throw new IllegalArgumentException("⚠ La data di fine deve essere successiva a quella di inizio");
        }
    }

    /**
     * Controlla che l’indirizzo non sia vuoto
     */
    public static void validaLuogo(String indirizzo) {
        if (indirizzo == null || indirizzo.isBlank()) {
            throw new IllegalArgumentException("⚠ Indirizzo mancante o vuoto");
        }
    }

    /**
     * Controlla che il prezzo sia non negativo
     */
    public static void validaPrezzo(double prezzo) {
        if (prezzo < 0) {
            throw new IllegalArgumentException("⚠ Il prezzo deve essere maggiore o uguale a zero");
        }
    }

    /**
     * Controlla che ci sia almeno 1 partecipante minimo
     */
    public static void validaMinPartecipanti(int min) {
        if (min < 1) {
            throw new IllegalArgumentException("⚠ Il numero minimo di partecipanti deve essere almeno 1");
        }
    }
}
