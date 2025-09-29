package unicam.filiera.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO per visualizzare un acquisto nella lista storica (vista "master").
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AcquistoListaDto {
    private Long id;
    private String usernameAcquirente;
    private double totale;
    private String statoPagamento;       // es. APPROVATO / RIFIUTATO
    private String tipoMetodoPagamento;  // es. CARTA_DI_CREDITO
    private LocalDateTime dataOra;
    private String elencoItem;           // stringa riassuntiva tipo "Mela x2, Pacchetto A x1"
}
