package unicam.filiera.dto;

import lombok.*;

/**
 * DTO che rappresenta i totali globali del carrello (quantità + costo totale).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class CartTotalsDto {
    private int totaleArticoli;
    private double costoTotale;
}
