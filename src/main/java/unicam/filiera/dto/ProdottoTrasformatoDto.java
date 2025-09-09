package unicam.filiera.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * DTO per la creazione o modifica di un Prodotto Trasformato da parte del Trasformatore.
 * La cardinalità (>= 2 fasi complete) è verificata nei controller/service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProdottoTrasformatoDto extends BaseItemDto {

    /**
     * Elenco delle fasi di produzione del prodotto trasformato.
     * Validazione su numero e completezza nei controller/service.
     */
    private List<FaseProduzioneDto> fasiProduzione;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class FaseProduzioneDto {

        @NotBlank(message = "⚠ Descrizione fase obbligatoria")
        private String descrizioneFase;

        @NotBlank(message = "⚠ Devi selezionare un produttore")
        private String produttoreUsername;

        @NotNull(message = "⚠ Devi selezionare un prodotto base")
        private Long prodottoOrigineId;
    }
}
