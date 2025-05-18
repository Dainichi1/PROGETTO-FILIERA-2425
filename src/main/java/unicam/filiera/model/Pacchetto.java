package unicam.filiera.model;

import java.util.List;

/**
 * Un insieme di almeno due prodotti offerto come pacchetto.
 * Condivide con {@link Prodotto} i campi definiti in {@link Item}.
 */
public class Pacchetto extends Item {

    /* ---- campi specifici ---- */
    private final double prezzoTotale;
    private final int quantita;
    private final List<Prodotto> prodotti;

    /* ---- costruttore privato (invocato dal Builder) ---- */
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
        this.prezzoTotale = b.prezzoTotale;
        this.prodotti = List.copyOf(b.prodotti);
        this.quantita = b.quantita;
    }

    /* ---- getter specifici ---- */
    public double getPrezzoTotale() {
        return prezzoTotale;
    }

    public int getQuantita() {
        return quantita;
    }


    public List<Prodotto> getProdotti() {
        return prodotti;
    }

    /* ------------------------------------------------------------------ */

    /**
     * Builder interno
     */
    public static class Builder {
        /* ---- campi comuni (Item) ---- */
        private String nome;
        private String descrizione;
        private int quantita;

        private String indirizzo;
        private List<String> certificati;
        private List<String> foto;
        private String creatoDa;
        private StatoProdotto stato;
        private String commento;

        /* ---- campi specifici (Pacchetto) ---- */
        private double prezzoTotale;
        private List<Prodotto> prodotti;

        /* ---------- metodi ‘with’ ---------- */
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

        public Builder quantita(int q) {
            this.quantita = q;
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

        public Builder prezzoTotale(double p) {
            this.prezzoTotale = p;
            return this;
        }

        public Builder prodotti(List<Prodotto> p) {
            this.prodotti = p;
            return this;
        }

        /* ---------- build() ---------- */
        public Pacchetto build() {
            if (nome == null || descrizione == null || indirizzo == null
                    || prodotti == null || prodotti.size() < 2)
                throw new IllegalStateException(
                        "Pacchetto non valido: nome, descrizione, indirizzo e almeno 2 prodotti sono obbligatori"
                );
            return new Pacchetto(this);
        }
    }
    /* ------------------------------------------------------------------ */

    @Override
    public String toString() {
        return String.format(
                "Pacchetto[nome=%s, prodotti=%d, prezzoTot=%.2f €, quantita=%d, stato=%s]",
                getNome(),
                prodotti != null ? prodotti.size() : 0,
                prezzoTotale,
                quantita, // <-- aggiunto qui
                getStato() != null ? getStato().name() : "N/D"
        );
    }

}
