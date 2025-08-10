package unicam.filiera.dto;

import java.time.LocalDateTime;

public class PostSocialDto {
    private long id;
    private String autoreUsername;
    private Integer idAcquisto;    // può essere null per post generici
    private String nomeItem;
    private String tipoItem;       // "Prodotto" | "Pacchetto" | ...
    private String titolo;
    private String testo;

    private LocalDateTime createdAt;

    // Costruttori
    public PostSocialDto() {
    }

    public PostSocialDto(String autoreUsername, Integer idAcquisto, String nomeItem,
                         String tipoItem, String titolo, String testo) {
        this.autoreUsername = autoreUsername;
        this.idAcquisto = idAcquisto;
        this.nomeItem = nomeItem;
        this.tipoItem = tipoItem;
        this.titolo = titolo;
        this.testo = testo;

    }

    // Getter/Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAutoreUsername() {
        return autoreUsername;
    }

    public void setAutoreUsername(String autoreUsername) {
        this.autoreUsername = autoreUsername;
    }

    public Integer getIdAcquisto() {
        return idAcquisto;
    }

    public void setIdAcquisto(Integer idAcquisto) {
        this.idAcquisto = idAcquisto;
    }

    public String getNomeItem() {
        return nomeItem;
    }

    public void setNomeItem(String nomeItem) {
        this.nomeItem = nomeItem;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
