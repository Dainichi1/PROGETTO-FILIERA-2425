package unicam.filiera.model;

import lombok.Getter;
import unicam.filiera.dto.ItemTipo;

import java.util.Collections;
import java.util.List;

/**
 * Un prodotto trasformato realizzato da un Trasformatore,
 * che contiene una sequenza di fasi di produzione collegate ai Produttori e ai loro Prodotti.
 */
@Getter
public class ProdottoTrasformato extends Item {

    private final Long id;

    // Fasi di produzione (almeno 2) -> immutabile
    private final List<FaseProduzione> fasiProduzione;

    // Quantità totale prodotta (es. lotti)
    private final int quantita;

    // Prezzo totale
    private final double prezzo;

    /* --- Costruttore privato, invocato dal Builder --- */
    private ProdottoTrasformato(Builder b) {
        super(
                b.nome,
                b.descrizione,
                b.indirizzo,
                b.certificati == null ? List.of() : Collections.unmodifiableList(b.certificati),
                b.foto == null ? List.of() : Collections.unmodifiableList(b.foto),
                b.creatoDa,
                b.stato,
                b.commento,
                ItemTipo.TRASFORMATO
        );
        this.id = b.id;
        this.quantita = b.quantita;
        this.prezzo = b.prezzo;

        // Copia difensiva + immutabilità
        this.fasiProduzione = (b.fasiProduzione == null)
                ? List.of()
                : Collections.unmodifiableList(List.copyOf(b.fasiProduzione));
    }

    /* --- Builder --- */
    public static class Builder {
        // Campi comuni (Item)
        private Long id;
        private String nome;
        private String descrizione;
        private String indirizzo;
        private List<String> certificati;
        private List<String> foto;
        private String creatoDa;
        private StatoProdotto stato;
        private String commento;

        // Campi specifici
        private List<FaseProduzione> fasiProduzione;
        private int quantita;
        private double prezzo;

        public Builder id(Long i) { this.id = i; return this; }
        public Builder nome(String n) { this.nome = n; return this; }
        public Builder descrizione(String d) { this.descrizione = d; return this; }
        public Builder indirizzo(String i) { this.indirizzo = i; return this; }
        public Builder certificati(List<String> c) { this.certificati = c; return this; }
        public Builder foto(List<String> f) { this.foto = f; return this; }
        public Builder creatoDa(String u) { this.creatoDa = u; return this; }
        public Builder stato(StatoProdotto s) { this.stato = s; return this; }
        public Builder commento(String c) { this.commento = c; return this; }
        public Builder fasiProduzione(List<FaseProduzione> fasi) { this.fasiProduzione = fasi; return this; }
        public Builder quantita(int q) { this.quantita = q; return this; }
        public Builder prezzo(double p) { this.prezzo = p; return this; }

        public ProdottoTrasformato build() {
            if (nome == null || descrizione == null || creatoDa == null || stato == null)
                throw new IllegalStateException("Campi obbligatori mancanti");
            if (fasiProduzione == null || fasiProduzione.size() < 2)
                throw new IllegalStateException("Un prodotto trasformato deve contenere almeno 2 fasi di produzione");
            if (quantita <= 0)
                throw new IllegalStateException("La quantità deve essere maggiore di 0");
            if (prezzo <= 0)
                throw new IllegalStateException("Il prezzo deve essere maggiore di 0");
            return new ProdottoTrasformato(this);
        }
    }

    @Override
    public String toString() {
        return """
                Prodotto Trasformato:
                   - Nome: %s
                   - Descrizione: %s
                   - Quantità: %d
                   - Prezzo: %.2f €
                   - Fasi di produzione: %s
                   - Certificati: %s
                   - Foto: %s
                   - Creato da: %s
                   - Stato: %s
                   - Commento: %s
                   - Indirizzo: %s
            """.formatted(
                getNome(),
                getDescrizione(),
                quantita,
                prezzo,
                fasiProduzione != null ? fasiProduzione.toString() : "Nessuna",
                getCertificati() != null ? String.join(", ", getCertificati()) : "Nessuno",
                getFoto() != null ? String.join(", ", getFoto()) : "Nessuna",
                getCreatoDa(),
                getStato() != null ? getStato().name() : "N/D",
                getCommento() != null ? getCommento() : "N/D",
                getIndirizzo() != null ? getIndirizzo() : "N/D"
        );
    }
}
