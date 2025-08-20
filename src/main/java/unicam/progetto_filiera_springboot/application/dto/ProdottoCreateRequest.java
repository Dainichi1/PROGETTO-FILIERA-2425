package unicam.progetto_filiera_springboot.application.dto;

import jakarta.validation.constraints.*;

public class ProdottoCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String nome;

    @Size(max = 1000)
    private String descrizione;

    @PositiveOrZero
    private int quantita = 0;

    @NotNull
    @Digits(integer = 8, fraction = 2)
    private java.math.BigDecimal prezzo;

    @Size(max = 255)
    private String indirizzo;

    @NotBlank
    private String creatoDaUsername;

    // getter/setter
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public java.math.BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(java.math.BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getCreatoDaUsername() {
        return creatoDaUsername;
    }

    public void setCreatoDaUsername(String creatoDaUsername) {
        this.creatoDaUsername = creatoDaUsername;
    }
}
