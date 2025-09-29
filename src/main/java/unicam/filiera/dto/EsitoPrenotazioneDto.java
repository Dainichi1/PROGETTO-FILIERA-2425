package unicam.filiera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EsitoPrenotazioneDto {
    private boolean success;
    private String message;
    private Double nuoviFondi; // può essere null se non serve
}
