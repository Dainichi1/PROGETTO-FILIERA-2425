package unicam.filiera.model;

import java.util.List;

public class Pacchetto {
    private String nome;
    private String descrizione;
    private String indirizzo;
    private double prezzoTotale;
    private List<Prodotto> prodotti;
    private List<String> certificati;
    private List<String> foto;
    private StatoProdotto stato;
    private String commento;
    private String creatoDa;

    private Pacchetto(Builder builder) {
        this.nome = builder.nome;
        this.descrizione = builder.descrizione;
        this.indirizzo = builder.indirizzo;
        this.prezzoTotale = builder.prezzoTotale;
        this.prodotti = builder.prodotti;
        this.certificati = builder.certificati;
        this.foto = builder.foto;
        this.stato = builder.stato;
        this.commento = builder.commento;
        this.creatoDa = builder.creatoDa;
    }

    // Getters

    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
    public String getIndirizzo() { return indirizzo; }
    public double getPrezzoTotale() { return prezzoTotale; }
    public List<Prodotto> getProdotti() { return prodotti; }
    public List<String> getCertificati() { return certificati; }
    public List<String> getFoto() { return foto; }
    public StatoProdotto getStato() { return stato; }
    public String getCommento() { return commento; }
    public String getCreatoDa() { return creatoDa; }

    public static class Builder {
        private String nome;
        private String descrizione;
        private String indirizzo;
        private double prezzoTotale;
        private List<Prodotto> prodotti;
        private List<String> certificati;
        private List<String> foto;
        private StatoProdotto stato;
        private String commento;
        private String creatoDa;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder descrizione(String descrizione) {
            this.descrizione = descrizione;
            return this;
        }

        public Builder indirizzo(String indirizzo) {
            this.indirizzo = indirizzo;
            return this;
        }

        public Builder prezzoTotale(double prezzoTotale) {
            this.prezzoTotale = prezzoTotale;
            return this;
        }

        public Builder prodotti(List<Prodotto> prodotti) {
            this.prodotti = prodotti;
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

        public Builder stato(StatoProdotto stato) {
            this.stato = stato;
            return this;
        }

        public Builder commento(String commento) {
            this.commento = commento;
            return this;
        }

        public Builder creatoDa(String creatoDa) {
            this.creatoDa = creatoDa;
            return this;
        }

        public Pacchetto build() {
            if (nome == null || descrizione == null || prodotti == null || prodotti.size() < 2)
                throw new IllegalStateException("Pacchetto non valido: nome, descrizione e almeno 2 prodotti sono obbligatori");
            return new Pacchetto(this);
        }
    }

    /** Permette al Curatore di aggiornare lo stato */
    public void setStato(StatoProdotto stato) {
        this.stato = stato;
    }

    /** Permette al Curatore di lasciare un commento di rifiuto */
    public void setCommento(String commento) {
        this.commento = commento;
    }

    @Override
    public String toString() {
        return """
                Pacchetto:
                  - Nome: %s
                  - Descrizione: %s
                  - Prezzo Totale: %.2f €
                  - Indirizzo: %s
                  - Prodotti: %s
                  - Certificati: %s
                  - Foto: %s
                  - Stato: %s
                  - Commento: %s
                  - Creato da: %s
                """.formatted(
                nome,
                descrizione,
                prezzoTotale,
                indirizzo,
                prodotti != null ? prodotti.size() + " prodotti" : "Nessuno",
                certificati != null ? String.join(", ", certificati) : "Nessuno",
                foto != null ? String.join(", ", foto) : "Nessuna",
                stato != null ? stato.name() : "N/D",
                commento != null ? commento : "N/D",
                creatoDa
        );
    }
}
