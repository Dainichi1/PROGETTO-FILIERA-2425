package unicam.filiera.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO per la creazione o modifica di un Pacchetto da parte del Distributore.
 * La cardinalità (>= 2 prodotti APPROVATI) è verificata nei controller/service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PacchettoDto extends BaseItemDto {

    /**
     * Prodotti selezionati che compongono il pacchetto (>= 2).
     */
    @Size(min = 2, message = "⚠ Devi selezionare almeno 2 prodotti approvati")
    private java.util.List<Long> prodottiSelezionati;
}
