package unicam.filiera.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO per la prenotazione di un ingresso a una fiera da parte di un Acquirente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PrenotazioneFieraDto {

    @EqualsAndHashCode.Include
    private Long id;  // id prenotazione (solo lettura, non in creazione)

    @NotNull(message = "⚠ ID fiera obbligatorio")
    private Long idFiera;

    @NotNull(message = "⚠ Devi inserire il numero di persone")
    @Min(value = 1, message = "⚠ Devi inserire almeno 1 persona")
    private Integer numeroPersone;

    private String usernameAcquirente; // valorizzato automaticamente da Spring Security

    private LocalDateTime dataPrenotazione; // generato dal sistema in fase di salvataggio

    // Campi aggiuntivi solo per la vista (non obbligatori in input)
    private String nomeFiera;
    private Double prezzoFiera;

    // Costruttore custom usato in mapToDto
    public PrenotazioneFieraDto(Long id,
                                Long idFiera,
                                Integer numeroPersone,
                                String usernameAcquirente,
                                LocalDateTime dataPrenotazione) {
        this.id = id;
        this.idFiera = idFiera;
        this.numeroPersone = numeroPersone;
        this.usernameAcquirente = usernameAcquirente;
        this.dataPrenotazione = dataPrenotazione;
    }
}
