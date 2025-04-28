package unicam.filiera.model;

import java.util.List;

public class Prodotto {
    private String nome;
    private String descrizione;
    private int quantita;
    private double prezzo;
    private List<String> certificati;
    private List<String> foto;
    private String creatoDa;
    private StatoProdotto stato;
    private String commento;
    private String indirizzo;


    // Costruttore privato usato solo dal Builder
    private Prodotto(Builder builder) {
        this.nome = builder.nome;
        this.descrizione = builder.descrizione;
        this.quantita = builder.quantita;
        this.prezzo = builder.prezzo;
        this.certificati = builder.certificati;
        this.foto = builder.foto;
        this.creatoDa = builder.creatoDa;
        this.stato = builder.stato;
        this.commento = builder.commento;
        this.indirizzo = builder.indirizzo;

    }

    // Getter e Setter
    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public int getQuantita() {
        return quantita;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public List<String> getCertificati() {
        return certificati;
    }

    public List<String> getFoto() {
        return foto;
    }

    public String getCreatoDa() {
        return creatoDa;
    }

    public StatoProdotto getStato() {
        return stato;
    }

    public String getCommento() {
        return commento;
    }

    public String getIndirizzo() {
        return indirizzo;
    }


    public void setCommento(String commento) {
        this.commento = commento;
    }

    // Builder interno statico
    public static class Builder {
        private String nome;
        private String descrizione;
        private int quantita;
        private double prezzo;
        private List<String> certificati;
        private List<String> foto;
        private String creatoDa;
        private StatoProdotto stato;
        private String commento;
        private String indirizzo;

        public Builder indirizzo(String indirizzo) {
            this.indirizzo = indirizzo;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder descrizione(String descrizione) {
            this.descrizione = descrizione;
            return this;
        }

        public Builder quantita(int quantita) {
            this.quantita = quantita;
            return this;
        }

        public Builder prezzo(double prezzo) {
            this.prezzo = prezzo;
            return this;
        }

        public Builder certificati(List<String> certificati) {
            this.certificati = certificati;
            return this;
        }

        public Builder foto(List<String> foto) {
            this.foto = foto;
            return this;
        }

        public Builder creatoDa(String creatoDa) {
            this.creatoDa = creatoDa;
            return this;
        }

        public Builder stato(StatoProdotto stato) {
            this.stato = stato;
            return this;
        }

        public Builder commento(String commento) {
            this.commento = commento;
            return this;
        }

        public Prodotto build() {
            // (Facoltativo) Validazioni
            if (nome == null || descrizione == null || creatoDa == null || stato == null)
                throw new IllegalStateException("Campi obbligatori mancanti");
            return new Prodotto(this);
        }
    }

    @Override
    public String toString() {
        return """
                Prodotto:
                  - Nome: %s
                  - Descrizione: %s
                  - Quantità: %d
                  - Prezzo: %.2f €
                  - Certificati: %s
                  - Foto: %s
                  - Creato da: %s
                  - Stato: %s
                  - Commento: %s
                  - Indirizzo: %s
                """.formatted(
                nome,
                descrizione,
                quantita,
                prezzo,
                certificati != null ? String.join(", ", certificati) : "Nessuno",
                foto != null ? String.join(", ", foto) : "Nessuna",
                creatoDa,
                stato != null ? stato.name() : "N/D",
                commento != null ? commento : "N/D",
                indirizzo != null ? indirizzo : "N/D"
        );
    }
}
