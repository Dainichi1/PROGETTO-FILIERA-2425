package unicam.filiera.model;

import lombok.Getter;
import unicam.filiera.dto.ItemTipo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Un insieme di almeno due prodotti offerto come pacchetto.
 * Estende {@link Item} ereditandone campi e logica comuni.
 */
@Getter
public class Pacchetto extends Item {

    /* --- campi specifici --- */
    private final Long id;
    private final int quantita;
    private final double prezzo;
    private final List<Long> prodottiIds; // <-- ID dei prodotti inclusi

    /* --- costruttore privato, invocato dal Builder --- */
    private Pacchetto(Builder b) {
        super(
                b.nome,
                b.descrizione,
                b.indirizzo,
                b.certificati == null ? List.of() : Collections.unmodifiableList(List.copyOf(b.certificati)),
                b.foto == null ? List.of() : Collections.unmodifiableList(List.copyOf(b.foto)),
                b.creatoDa,
                b.stato,
                b.commento,
                ItemTipo.PACCHETTO
        );
        this.id = b.id;
        this.quantita = b.quantita;
        this.prezzo = b.prezzo;
        // copia difensiva + non modificabile
        this.prodottiIds = (b.prodottiIds == null)
                ? List.of()
                : Collections.unmodifiableList(List.copyOf(b.prodottiIds));
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
        private Long id;
        private int quantita;
        private double prezzo;
        private List<Long> prodottiIds; // <-- Long

        /* ------- metodi ‘with’ ------- */
        public Builder id(Long id) { this.id = id; return this; }
        public Builder nome(String n) { this.nome = n; return this; }
        public Builder descrizione(String d) { this.descrizione = d; return this; }
        public Builder indirizzo(String i) { this.indirizzo = i; return this; }
        public Builder certificati(List<String> c) { this.certificati = c; return this; }
        public Builder foto(List<String> f) { this.foto = f; return this; }
        public Builder creatoDa(String u) { this.creatoDa = u; return this; }
        public Builder stato(StatoProdotto s) { this.stato = s; return this; }
        public Builder commento(String c) { this.commento = c; return this; }
        public Builder quantita(int q) { this.quantita = q; return this; }
        public Builder prezzo(double p) { this.prezzo = p; return this; }
        public Builder prodottiIds(List<Long> ids) { this.prodottiIds = ids; return this; }

        /* ------- build() ------- */
        public Pacchetto build() {
            if (nome == null || descrizione == null || creatoDa == null || stato == null)
                throw new IllegalStateException("Campi obbligatori mancanti");
            if (prodottiIds == null || prodottiIds.stream().filter(Objects::nonNull).distinct().count() < 2)
                throw new IllegalStateException("Un pacchetto deve contenere almeno 2 prodotti");
            if (quantita < 0)
                throw new IllegalStateException("La quantità non può essere negativa");
            if (prezzo <= 0)
                throw new IllegalStateException("Il prezzo deve essere maggiore di 0");
            return new Pacchetto(this);
        }
    }

    /* ------------------------------------------------------------------ */

    @Override
    public String toString() {
        return String.format(
                "Pacchetto[nome=%s, prodotti=%d, prezzo=%.2f €, quantita=%d, stato=%s]",
                getNome(),
                prodottiIds != null ? prodottiIds.size() : 0,
                prezzo,
                quantita,
                getStato() != null ? getStato().name() : "N/D"
        );
    }
}
