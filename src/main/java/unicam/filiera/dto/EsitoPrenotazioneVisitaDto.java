package unicam.filiera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EsitoPrenotazioneVisitaDto {
    private boolean success;
    private String message;
}
