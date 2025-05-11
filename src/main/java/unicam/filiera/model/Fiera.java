// -------- Fiera.java --------
package unicam.filiera.model;

import java.time.LocalDateTime;

/**
 * Rappresenta una fiera o un evento aperto al pubblico,
 * con un numero minimo di partecipanti richiesto.
 */
public class Fiera extends Evento {
    private final String organizzatore;
    private final int numeroMinPartecipanti;

    private Fiera(Builder b) {
        super(
                b.id,
                b.dataInizio,
                b.dataFine,
                b.prezzo,
                b.descrizione,
                b.indirizzo,
                b.stato
        );
        this.organizzatore = b.organizzatore;
        this.numeroMinPartecipanti = b.numeroMinPartecipanti;
    }

    public String getOrganizzatore() {
        return organizzatore;
    }

    public int getNumeroMinPartecipanti() {
        return numeroMinPartecipanti;
    }

    /**
     * eredita getStato() da Evento
     */

    public boolean raggiungeMinimo(int partecipantiAttuali) {
        return partecipantiAttuali >= this.numeroMinPartecipanti;
    }

    @Override
    public String toString() {
        return String.format(
                "Fiera[id=%d, %s → %s, prezzo=%.2f, minPartecipanti=%d, stato=%s, organizzatore=%s]",
                getId(),
                getDataInizio(),
                getDataFine(),
                getPrezzo(),
                numeroMinPartecipanti,
                getStato(),
                organizzatore
        );
    }

    // --- Builder interno ---
    public static class Builder {
        private long id;
        private LocalDateTime dataInizio;
        private LocalDateTime dataFine;
        private double prezzo;
        private String descrizione;
        private String indirizzo;
        private String organizzatore;
        private int numeroMinPartecipanti;
        private StatoEvento stato = StatoEvento.IN_PREPARAZIONE;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder dataInizio(LocalDateTime dt) {
            this.dataInizio = dt;
            return this;
        }

        public Builder dataFine(LocalDateTime dt) {
            this.dataFine = dt;
            return this;
        }

        public Builder prezzo(double p) {
            this.prezzo = p;
            return this;
        }

        public Builder descrizione(String d) {
            this.descrizione = d;
            return this;
        }

        public Builder indirizzo(String i) {
            this.indirizzo = i;
            return this;
        }

        public Builder organizzatore(String o) {
            this.organizzatore = o;
            return this;
        }

        public Builder numeroMinPartecipanti(int n) {
            this.numeroMinPartecipanti = n;
            return this;
        }

        public Builder stato(StatoEvento s) {
            this.stato = s;
            return this;
        }

        public Fiera build() {
            if (dataInizio == null || dataFine == null
                    || descrizione == null || indirizzo == null
                    || organizzatore == null)
                throw new IllegalStateException("Campi obbligatori mancanti per Fiera");
            return new Fiera(this);
        }
    }
}
