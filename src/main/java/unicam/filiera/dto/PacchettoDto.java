package unicam.filiera.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO per la creazione o modifica di un Pacchetto da parte del distributore.
 * Incapsula tutti i campi del form della UI.
 */
@Getter
@Setter
@NoArgsConstructor
public class PacchettoDto {

    private String originalName;

    @NotBlank(message = "⚠ Nome pacchetto obbligatorio")
    private String nome;

    @NotBlank(message = "⚠ Descrizione obbligatoria")
    private String descrizione;

    @NotBlank(message = "⚠ Indirizzo luogo vendita obbligatorio")
    private String indirizzo;

    @NotNull(message = "⚠ Inserisci un prezzo")
    @Positive(message = "⚠ Il prezzo deve essere positivo")
    private Double prezzo;

    @NotNull(message = "⚠ Inserisci la quantità")
    @Min(value = 1, message = "⚠ La quantità deve essere almeno 1")
    private Integer quantita;

    @NotEmpty(message = "⚠ Devi selezionare almeno 2 prodotti")
    @Size(min = 2, message = "⚠ Devi selezionare almeno 2 prodotti")
    private List<Long> prodottiSelezionati;

    // Validazione gestita manualmente nel Controller
    private List<MultipartFile> certificati;
    private List<MultipartFile> foto;

    public PacchettoDto(
            String nome,
            String descrizione,
            String indirizzo,
            Double prezzo,
            Integer quantita,
            List<Long> prodottiSelezionati,
            List<MultipartFile> certificati,
            List<MultipartFile> foto
    ) {
        this(null, nome, descrizione, indirizzo, prezzo, quantita, prodottiSelezionati, certificati, foto);
    }

    public PacchettoDto(
            String originalName,
            String nome,
            String descrizione,
            String indirizzo,
            Double prezzo,
            Integer quantita,
            List<Long> prodottiSelezionati,
            List<MultipartFile> certificati,
            List<MultipartFile> foto
    ) {
        this.originalName = originalName;
        this.nome = nome;
        this.descrizione = descrizione;
        this.indirizzo = indirizzo;
        this.prezzo = prezzo;
        this.quantita = quantita;
        this.prodottiSelezionati = prodottiSelezionati;
        this.certificati = certificati;
        this.foto = foto;
    }
}
