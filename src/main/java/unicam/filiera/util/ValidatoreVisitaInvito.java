package unicam.filiera.util;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Validatore delle visite su invito.
 * Contiene le stesse regole di ValidatoreFiera, più il controllo sui destinatari.
 */
public class ValidatoreVisitaInvito {

    /**
     * Controlla che la data di fine sia dopo quella di inizio
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

    /**
     * Controlla che la lista di destinatari non sia vuota
     */
    public static void validaDestinatari(List<String> destinatari) {
        if (destinatari == null || destinatari.isEmpty()) {
            throw new IllegalArgumentException("⚠ Devi specificare almeno un destinatario");
        }
        // eventualmente controlla che nessuna stringa sia vuota
        for (String d : destinatari) {
            if (d == null || d.isBlank()) {
                throw new IllegalArgumentException("⚠ Uno dei destinatari è vuoto o non valido");
            }
        }
    }
}
