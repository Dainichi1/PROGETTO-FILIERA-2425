package unicam.filiera.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO per la creazione o modifica di un Prodotto da parte del produttore.
 * Incapsula tutti i campi del form della UI.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProdottoDto {

    /**
     * Nel flusso di modifica, contiene il nome con cui il prodotto
     * era stato salvato originariamente (prima della correzione).
     * Altrimenti null.
     */
    private String originalName;

    @NotBlank(message = "⚠ Nome obbligatorio")
    private String nome;

    @NotBlank(message = "⚠ Descrizione obbligatoria")
    private String descrizione;

    @Min(value = 1, message = "⚠ La quantità deve essere almeno 1")
    private int quantita;

    @Positive(message = "⚠ Il prezzo deve essere positivo")
    private double prezzo;

    @NotBlank(message = "⚠ Indirizzo obbligatorio")
    private String indirizzo;

    // File caricati dal form (validazione gestita manualmente nel Controller)
    private List<MultipartFile> certificati;
    private List<MultipartFile> foto;

    // Costruttore personalizzato (utile nei test o conversioni manuali)
    public ProdottoDto(
            String nome,
            String descrizione,
            int quantita,
            double prezzo,
            String indirizzo,
            List<MultipartFile> certificati,
            List<MultipartFile> foto
    ) {
        this(null, nome, descrizione, quantita, prezzo, indirizzo, certificati, foto);
    }

    public ProdottoDto(
            String originalName,
            String nome,
            String descrizione,
            int quantita,
            double prezzo,
            String indirizzo,
            List<MultipartFile> certificati,
            List<MultipartFile> foto
    ) {
        this.originalName = originalName;
        this.nome = nome;
        this.descrizione = descrizione;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.indirizzo = indirizzo;
        this.certificati = certificati;
        this.foto = foto;
    }
}
