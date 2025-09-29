package unicam.filiera.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

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
     * Usato in fase di creazione/modifica.
     */
    @Size(min = 2, message = "⚠ Devi selezionare almeno 2 prodotti approvati")
    private List<Long> prodottiSelezionati;

    // ========== Campi aggiuntivi per gestione in lettura ==========

    /** Username del distributore che ha creato il pacchetto */
    private String creatoDa;

    /** Stato del pacchetto (IN_ATTESA, APPROVATO, RIFIUTATO) */
    private StatoProdotto stato;

    /** Eventuale commento del curatore */
    private String commento;

    /** Solo i nomi dei prodotti (per UI/marketplace) */
    private List<String> prodottiNomi;

    /** Solo gli ID dei prodotti (per UI/marketplace) */
    private List<Long> prodottiIds;
}
