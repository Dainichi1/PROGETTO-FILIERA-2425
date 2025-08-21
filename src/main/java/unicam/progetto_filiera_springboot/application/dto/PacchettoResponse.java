package unicam.progetto_filiera_springboot.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PacchettoResponse {

    private Long id;
    private String nome;
    private String descrizione;
    private int quantita;
    private BigDecimal prezzoTotale;
    private String indirizzo;
    private String creatoDa;
    private String stato;
    private String commento;
    private LocalDateTime createdAt;

    // CSV (es. "url1.jpg,url2.jpg")
    private String certificati;
    private String foto;

    // Prodotti inclusi nel pacchetto (nome o id, a tua scelta)
    private List<String> prodottiNomi;

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    public BigDecimal getPrezzoTotale() { return prezzoTotale; }
    public void setPrezzoTotale(BigDecimal prezzoTotale) { this.prezzoTotale = prezzoTotale; }

    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }

    public String getCreatoDa() { return creatoDa; }
    public void setCreatoDa(String creatoDa) { this.creatoDa = creatoDa; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public String getCommento() { return commento; }
    public void setCommento(String commento) { this.commento = commento; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCertificati() { return certificati; }
    public void setCertificati(String certificati) { this.certificati = certificati; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public List<String> getProdottiNomi() { return prodottiNomi; }
    public void setProdottiNomi(List<String> prodottiNomi) { this.prodottiNomi = prodottiNomi; }
}
