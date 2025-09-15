package unicam.filiera.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO per la prenotazione di una visita guidata da parte di un Venditore.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PrenotazioneVisitaDto {

    @EqualsAndHashCode.Include
    private Long id;  // id prenotazione (usato solo per lettura, non in creazione)

    @NotNull(message = "⚠ ID visita obbligatorio")
    private Long idVisita;

    @NotNull(message = "⚠ Devi inserire il numero di persone")
    @Min(value = 1, message = "⚠ Devi inserire almeno 1 persona")
    private Integer numeroPersone;

    private String usernameVenditore; // valorizzato automaticamente da Spring Security

    private LocalDateTime dataPrenotazione; // generato dal sistema in fase di salvataggio
}
