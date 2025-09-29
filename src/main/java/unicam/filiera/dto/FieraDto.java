package unicam.filiera.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import unicam.filiera.model.StatoEvento;

/**
 * DTO per la creazione o modifica di una Fiera da parte dell’Animatore.
 * Estende {@link BaseEventoDto}.
 *
 * Le fiere sono pubbliche e non hanno destinatari,
 * ma includono un campo aggiuntivo: prezzo.
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FieraDto extends BaseEventoDto {

    @DecimalMin(value = "0.0", inclusive = true, message = "⚠ Il prezzo non può essere negativo")
    private Double prezzo;

    /** Username dell'animatore che ha creato la fiera */
    private String creatoDa;

    /** Stato della fiera (PUBBLICATA, CONCLUSA, ecc.) */
    private StatoEvento stato;
}
