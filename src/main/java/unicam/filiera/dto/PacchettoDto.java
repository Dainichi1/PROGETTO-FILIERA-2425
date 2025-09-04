package unicam.filiera.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO per la creazione o modifica di un Pacchetto da parte del Distributore.
 * Un pacchetto è composto da almeno 2 prodotti approvati.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
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

    /**
     * Prodotti selezionati che compongono il pacchetto.
     * Devono essere almeno 2 e devono avere stato APPROVATO.
     */
    @Size(min = 2, message = "⚠ Devi selezionare almeno 2 prodotti")
    private List<Long> prodottiSelezionati;

    // File caricati (validazione gestita nei Controller)
    private List<MultipartFile> certificati;
    private List<MultipartFile> foto;
}
