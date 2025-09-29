package unicam.filiera.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import unicam.filiera.model.StatoPagamento;
import unicam.filiera.model.TipoMetodoPagamento;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO per rappresentare i dati di un acquisto completo.
 * Usato dal controller per confermare l’acquisto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatiAcquistoDto {

    private String usernameAcquirente;

    private double totaleAcquisto;

    private Double fondiPreAcquisto;
    private Double fondiPostAcquisto;

    private TipoMetodoPagamento tipoMetodoPagamento;
    private StatoPagamento statoPagamento;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private List<CartItemDto> items;
}
