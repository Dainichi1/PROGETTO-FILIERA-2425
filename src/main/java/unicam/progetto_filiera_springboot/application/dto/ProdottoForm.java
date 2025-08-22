package unicam.progetto_filiera_springboot.application.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;   // <— importa questo
import java.math.BigDecimal;
import java.util.List;

public class ProdottoForm {

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 100, message = "Max 100 caratteri")
    private String nome;

    @NotBlank(message = "La descrizione è obbligatoria")
    @Size(max = 1000, message = "Max 1000 caratteri")
    private String descrizione;

    @NotNull(message = "La quantità è obbligatoria")
    @PositiveOrZero(message = "La quantità deve essere ≥ 0")
    private Integer quantita;

    @NotNull(message = "Il prezzo è obbligatorio")
    @Digits(integer = 8, fraction = 2, message = "Prezzo non valido (max 8 cifre, 2 decimali)")
    @DecimalMin(value = "0.00", message = "Il prezzo deve essere ≥ 0")
    private BigDecimal prezzo;

    @NotBlank(message = "L'indirizzo è obbligatorio")
    @Size(max = 255, message = "Max 255 caratteri")
    private String indirizzo;


    private List<MultipartFile> foto;
    private List<MultipartFile> certificati;

    // getter/setter
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public Integer getQuantita() { return quantita; }
    public void setQuantita(Integer quantita) { this.quantita = quantita; }
    public BigDecimal getPrezzo() { return prezzo; }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo = prezzo; }
    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }

    public List<MultipartFile> getFoto() { return foto; }
    public void setFoto(List<MultipartFile> foto) { this.foto = foto; }
    public List<MultipartFile> getCertificati() { return certificati; }
    public void setCertificati(List<MultipartFile> certificati) { this.certificati = certificati; }
}
