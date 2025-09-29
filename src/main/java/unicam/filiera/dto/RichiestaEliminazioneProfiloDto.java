package unicam.filiera.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO che rappresenta una richiesta di eliminazione profilo.
 * Usato dal Gestore per visualizzare e gestire le richieste.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RichiestaEliminazioneProfiloDto {

    private Long id;                  // ID univoco della richiesta
    private String username;          // Utente che ha richiesto l’eliminazione
    private String stato;             // Stato attuale (IN_ATTESA, APPROVATA, RIFIUTATA)
    private LocalDateTime dataRichiesta; // Data e ora della richiesta
}
