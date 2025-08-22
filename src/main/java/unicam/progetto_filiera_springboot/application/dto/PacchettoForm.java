package unicam.progetto_filiera_springboot.application.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public class PacchettoForm {

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 100, message = "Max 100 caratteri")
    private String nome;

    @NotBlank(message = "La descrizione è obbligatoria")
    @Size(max = 1000, message = "Max 1000 caratteri")
    private String descrizione;

    @NotBlank(message = "L'indirizzo è obbligatorio")
    @Size(max = 255, message = "Max 255 caratteri")
    private String indirizzo;

    @NotNull(message = "Il prezzo totale è obbligatorio")
    @Digits(integer = 8, fraction = 2, message = "Prezzo non valido (max 8 cifre, 2 decimali)")
    @DecimalMin(value = "0.00", message = "Il prezzo deve essere ≥ 0")
    private BigDecimal prezzoTotale;

    @NotNull(message = "La quantità è obbligatoria")
    @Positive(message = "La quantità deve essere ≥ 1")
    private Integer quantita;

    @NotNull(message = "Seleziona almeno due prodotti")
    @Size(min = 2, message = "Devi selezionare almeno due prodotti")
    private List<Long> prodottiIds;

    private List<MultipartFile> foto;
    private List<MultipartFile> certificati;

    // getter/setter
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }

    public BigDecimal getPrezzoTotale() { return prezzoTotale; }
    public void setPrezzoTotale(BigDecimal prezzoTotale) { this.prezzoTotale = prezzoTotale; }

    public Integer getQuantita() { return quantita; }
    public void setQuantita(Integer quantita) { this.quantita = quantita; }

    public List<Long> getProdottiIds() { return prodottiIds; }
    public void setProdottiIds(List<Long> prodottiIds) { this.prodottiIds = prodottiIds; }

    public List<MultipartFile> getFoto() { return foto; }
    public void setFoto(List<MultipartFile> foto) { this.foto = foto; }
    public List<MultipartFile> getCertificati() { return certificati; }
    public void setCertificati(List<MultipartFile> certificati) { this.certificati = certificati; }
}
