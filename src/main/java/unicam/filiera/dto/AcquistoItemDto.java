package unicam.filiera.dto;

import lombok.*;

/**
 * DTO che rappresenta un singolo item incluso in un acquisto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AcquistoItemDto {
    private String nomeItem;
    private String tipoItem;     // PRODOTTO o PACCHETTO (da ItemTipo)
    private int quantita;
    private double prezzoUnitario;
    private double totale;
}
