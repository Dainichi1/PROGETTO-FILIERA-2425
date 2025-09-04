package unicam.filiera.model;

import lombok.Getter;

import java.util.List;

/**
 * Un insieme di almeno due prodotti offerto come pacchetto.
 * <p>Estende {@link Item} ereditandone campi e logica comuni.</p>
 */
@Getter
public class Pacchetto extends Item {

    /* --- campi specifici --- */
    private final int quantita;
    private final double prezzo;
    private final List<String> prodotti; // lista di ID o nomi prodotti inclusi

    /* --- costruttore privato, invocato dal Builder --- */
    private Pacchetto(Builder b) {
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
        this.prodotti = b.prodotti;
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

        /* campi specifici (Pacchetto) */
        private int quantita;
        private double prezzo;
        private List<String> prodotti;

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

        public Builder prodotti(List<String> p) {
            this.prodotti = p;
            return this;
        }

        /* ------- build() ------- */
        public Pacchetto build() {
            if (nome == null || descrizione == null || creatoDa == null || stato == null)
                throw new IllegalStateException("Campi obbligatori mancanti");
            if (prodotti == null || prodotti.size() < 2)
                throw new IllegalStateException("Un pacchetto deve contenere almeno 2 prodotti");
            return new Pacchetto(this);
        }
    }

    /* ------------------------------------------------------------------ */

    @Override
    public String toString() {
        return String.format(
                "Pacchetto[nome=%s, prodotti=%d, prezzo=%.2f €, quantita=%d, stato=%s]",
                getNome(),
                prodotti != null ? prodotti.size() : 0,
                prezzo,
                quantita,
                getStato() != null ? getStato().name() : "N/D"
        );
    }
}
