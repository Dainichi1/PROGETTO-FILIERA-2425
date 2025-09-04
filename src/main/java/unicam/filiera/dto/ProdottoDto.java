package unicam.filiera.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProdottoDto {

    private String originalName;

    @NotBlank(message = "⚠ Nome obbligatorio")
    private String nome;

    @NotBlank(message = "⚠ Descrizione obbligatoria")
    private String descrizione;

    @NotNull(message = "⚠ Inserisci la quantità")
    @Min(value = 1, message = "⚠ La quantità deve essere almeno 1")
    private Integer quantita;

    @NotNull(message = "⚠ Inserisci un prezzo")
    @Positive(message = "⚠ Il prezzo deve essere positivo")
    private Double prezzo;

    @NotBlank(message = "⚠ Indirizzo obbligatorio")
    private String indirizzo;

    // Validazione gestita manualmente nel Controller
    private List<MultipartFile> certificati;
    private List<MultipartFile> foto;

    public ProdottoDto(
            String nome,
            String descrizione,
            Integer quantita,
            Double prezzo,
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
            Integer quantita,
            Double prezzo,
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
