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
    }

    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
    public int getQuantita() { return quantita; }
    public double getPrezzo() { return prezzo; }
    public List<String> getCertificati() { return certificati; }
    public List<String> getFoto() { return foto; }
    public String getCreatoDa() { return creatoDa; }

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
        """.formatted(
                nome,
                descrizione,
                quantita,
                prezzo,
                certificati != null ? String.join(", ", certificati) : "Nessuno",
                foto != null ? String.join(", ", foto) : "Nessuna",
                creatoDa,
                stato != null ? stato.name() : "N/D"
        );
    }

    public StatoProdotto getStato() {
        return stato;
    }



}
