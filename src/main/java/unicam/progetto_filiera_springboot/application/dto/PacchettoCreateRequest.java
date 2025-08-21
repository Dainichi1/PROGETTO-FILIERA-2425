package unicam.progetto_filiera_springboot.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class PacchettoCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String nome;

    @Size(max = 1000)
    private String descrizione;

    @PositiveOrZero
    private int quantita = 0;

    @NotNull
    @Digits(integer = 8, fraction = 2)
    private BigDecimal prezzoTotale;

    @Size(max = 255)
    private String indirizzo;

    @NotBlank
    private String creatoDaUsername;

    @NotNull
    @Size(min = 2, message = "Seleziona almeno 2 prodotti per il pacchetto")
    private List<Long> prodottiIds;

    // getter/setter
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

    public String getCreatoDaUsername() { return creatoDaUsername; }
    public void setCreatoDaUsername(String creatoDaUsername) { this.creatoDaUsername = creatoDaUsername; }

    public List<Long> getProdottiIds() { return prodottiIds; }
    public void setProdottiIds(List<Long> prodottiIds) { this.prodottiIds = prodottiIds; }
}
