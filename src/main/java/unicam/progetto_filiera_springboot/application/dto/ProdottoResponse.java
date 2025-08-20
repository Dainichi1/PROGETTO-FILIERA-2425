package unicam.progetto_filiera_springboot.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProdottoResponse {
    private Long id;
    private String nome;
    private String descrizione;
    private int quantita;
    private BigDecimal prezzo;
    private String indirizzo;
    private String creatoDa;
    private String stato;
    private String commento;
    private LocalDateTime createdAt;

    // NEW: CSV di filename, es. "cert1.pdf,cert2.pdf"
    private String certificati;
    private String foto;

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    public BigDecimal getPrezzo() { return prezzo; }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo = prezzo; }

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
}
