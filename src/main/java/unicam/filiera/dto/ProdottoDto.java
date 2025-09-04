package unicam.filiera.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO per la creazione o modifica di un Prodotto da parte del Produttore.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
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

    // File caricati (validazione gestita nei Controller)
    private List<MultipartFile> certificati;
    private List<MultipartFile> foto;
}
