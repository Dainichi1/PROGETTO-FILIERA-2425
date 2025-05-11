package unicam.filiera.model;

import java.util.List;

/**
 * Un singolo prodotto messo in vendita.
 * <p>Estende {@link Item} ereditandone campi e logica comuni.</p>
 */
public class Prodotto extends Item {

    /* --- campi specifici --- */
    private final int quantita;
    private final double prezzo;

    /* --- costruttore privato, invocato dal Builder --- */
    private Prodotto(Builder b) {
        super(
                b.nome,
                b.descrizione,
                b.indirizzo,
                b.certificati,
                b.foto,
                b.creatoDa,
                b.stato,
                b.commento
        );
        this.quantita = b.quantita;
        this.prezzo = b.prezzo;
    }

    /* --- getter specifici --- */
    public int getQuantita() {
        return quantita;
    }

    public double getPrezzo() {
        return prezzo;
    }

    /* ------------------------------------------------------------------ */

    /**
     * Builder interno
     */
    public static class Builder {
        /* campi comuni (Item) */
        private String nome;
        private String descrizione;
        private String indirizzo;
        private List<String> certificati;
        private List<String> foto;
        private String creatoDa;
        private StatoProdotto stato;
        private String commento;

        /* campi specifici (Prodotto) */
        private int quantita;
        private double prezzo;

        /* ------- metodi ‘with’ ------- */
        public Builder nome(String n) {
            this.nome = n;
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

        public Builder certificati(List<String> c) {
            this.certificati = c;
            return this;
        }

        public Builder foto(List<String> f) {
            this.foto = f;
            return this;
        }

        public Builder creatoDa(String u) {
            this.creatoDa = u;
            return this;
        }

        public Builder stato(StatoProdotto s) {
            this.stato = s;
            return this;
        }

        public Builder commento(String c) {
            this.commento = c;
            return this;
        }

        public Builder quantita(int q) {
            this.quantita = q;
            return this;
        }

        public Builder prezzo(double p) {
            this.prezzo = p;
            return this;
        }

        /* ------- build() ------- */
        public Prodotto build() {
            if (nome == null || descrizione == null || creatoDa == null || stato == null)
                throw new IllegalStateException("Campi obbligatori mancanti");
            return new Prodotto(this);
        }
    }

    /* ------------------------------------------------------------------ */
    @Override
    public String toString() {
        return """
                 Prodotto:                 \s
                     - Nome: %s                 \s
                     - Descrizione: %s               \s
                     - Quantità: %d                \s
                     - Prezzo: %.2f €                \s
                     - Certificati: %s                 \s
                     - Foto: %s                \s
                     - Creato da: %s                \s
                     - Stato: %s                 \s
                     - Commento: %s                \s
                     - Indirizzo: %s              \s
                \s""".formatted(
                getNome(),
                getDescrizione(),
                quantita,
                prezzo,
                getCertificati() != null ? String.join(", ", getCertificati()) : "Nessuno",
                getFoto() != null ? String.join(", ", getFoto()) : "Nessuna",
                getCreatoDa(),
                getStato() != null ? getStato().name() : "N/D",
                getCommento() != null ? getCommento() : "N/D",
                getIndirizzo() != null ? getIndirizzo() : "N/D"
        );
    }
}
