package unicam.filiera.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO usato per la richiesta REST di aggiunta al carrello.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddToCartRequestDto {

    @NotNull(message = "⚠ Specifica il tipo di item (PRODOTTO, PACCHETTO, TRASFORMATO)")
    private ItemTipo tipo;   // enum invece di String

    @NotNull(message = "⚠ L'id dell'item è obbligatorio")
    private Long id;

    @Min(value = 1, message = "⚠ La quantità deve essere almeno 1")
    private int quantita;


}
