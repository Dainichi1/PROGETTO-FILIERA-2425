package unicam.filiera.dto;

import lombok.*;

/**
 * DTO per la creazione o modifica di un Prodotto da parte del Produttore.
 * Estende BaseItemDto.
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProdottoDto extends BaseItemDto {
    // Nessun campo aggiuntivo
}
