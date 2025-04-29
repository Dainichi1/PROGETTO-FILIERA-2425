package unicam.filiera.model;

import java.time.LocalDateTime;

/**
 * Sottoclasse di Evento che rappresenta una fiera aperta (prenotabile).
 */
public class Fiera extends Evento {

    private final int postiDisponibili;   // specifico della fiera

    /* ---------- costruttore privato ---------- */
    private Fiera(Builder b) {
        super(b);                         // inizializza la parte “Evento”
        this.postiDisponibili = b.postiDisponibili;
    }

    /* ---------- hook del Template Method ---------- */
    @Override
    protected void doPubblica() {
        // eventuale logica specifica alla fiera (logging, mail, ecc.)
    }

    public int getPostiDisponibili() { return postiDisponibili; }

    /* =================== Builder specifico =================== */
    public static class Builder extends Evento.Builder<Builder> {

        private int postiDisponibili = 0;

        public Builder postiDisponibili(int v){
            this.postiDisponibili = v; return this;
        }

        @Override protected Builder self() { return this; }

        public Fiera build() {
            // validazioni aggiuntive
            if (postiDisponibili < 0)
                throw new IllegalStateException("Posti disponibili negativi");
            return new Fiera(this);
        }
    }
}
