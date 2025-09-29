package unicam.filiera.dto;

import lombok.*;
import unicam.filiera.model.StatoProdotto;

/**
 * DTO per la creazione o modifica di un Prodotto da parte del Produttore.
 * Estende BaseItemDto e contiene campi aggiuntivi specifici per i prodotti.
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProdottoDto extends BaseItemDto {

    // Username del creatore del prodotto
    private String creatoDa;

    // Stato del prodotto (IN_ATTESA, APPROVATO, RIFIUTATO)
    private StatoProdotto stato;

    // Eventuale commento associato (es. curatore)
    private String commento;
}
