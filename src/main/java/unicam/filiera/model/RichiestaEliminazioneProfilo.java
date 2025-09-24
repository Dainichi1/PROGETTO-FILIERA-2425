package unicam.filiera.model;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Modello di dominio per la richiesta di eliminazione profilo.
 * Rappresenta la richiesta fatta da un utente, gestita dal Gestore della piattaforma.
 */
@Getter
@ToString
public class RichiestaEliminazioneProfilo {

    private final Long id;                          // ID univoco della richiesta
    private final String username;                  // Utente che ha richiesto l’eliminazione
    private final StatoRichiestaEliminazioneProfilo stato; // Stato della richiesta
    private final LocalDateTime dataRichiesta;      // Data e ora della richiesta

    private RichiestaEliminazioneProfilo(Builder b) {
        this.id = b.id;
        this.username = b.username;
        this.stato = b.stato;
        this.dataRichiesta = b.dataRichiesta;
    }

    /* ================== BUILDER ================== */
    public static class Builder {
        private Long id;
        private String username;
        private StatoRichiestaEliminazioneProfilo stato;
        private LocalDateTime dataRichiesta;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String u) { this.username = u; return this; }
        public Builder stato(StatoRichiestaEliminazioneProfilo s) { this.stato = s; return this; }
        public Builder dataRichiesta(LocalDateTime d) { this.dataRichiesta = d; return this; }

        public RichiestaEliminazioneProfilo build() {
            if (username == null || stato == null || dataRichiesta == null) {
                throw new IllegalStateException("⚠ Campi obbligatori mancanti per RichiestaEliminazioneProfilo");
            }
            return new RichiestaEliminazioneProfilo(this);
        }
    }
}
