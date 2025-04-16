package unicam.filiera.model;

import java.util.List;

public class Prodotto {
    private String nome;
    private String descrizione;
    private int quantita;
    private double prezzo;
    private List<String> certificati;
    private List<String> foto;
    private String creatoDa; // username o nome del produttore
    private StatoProdotto stato;

    // Nuovo campo per il commento
    private String commento;

    // Costruttore originale (senza commento)
    public Prodotto(String nome, String descrizione, int quantita, double prezzo,
                    List<String> certificati, List<String> foto, String creatoDa,
                    StatoProdotto stato) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.certificati = certificati;
        this.foto = foto;
        this.creatoDa = creatoDa;
        this.stato = stato;
        this.commento = null;  // di default null
    }

    // Costruttore aggiuntivo (con commento)
    public Prodotto(String nome, String descrizione, int quantita, double prezzo,
                    List<String> certificati, List<String> foto, String creatoDa,
                    StatoProdotto stato, String commento) {
        this(nome, descrizione, quantita, prezzo, certificati, foto, creatoDa, stato);
        this.commento = commento;
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

    public void setCommento(String commento) {
        this.commento = commento;
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
            """.formatted(
                nome,
                descrizione,
                quantita,
                prezzo,
                certificati != null ? String.join(", ", certificati) : "Nessuno",
                foto != null ? String.join(", ", foto) : "Nessuna",
                creatoDa,
                stato != null ? stato.name() : "N/D",
                commento != null ? commento : "N/D"
        );
    }
}
