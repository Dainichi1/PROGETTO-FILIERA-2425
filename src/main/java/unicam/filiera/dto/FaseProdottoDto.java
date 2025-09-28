package unicam.filiera.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FaseProdottoDto {
    private Long prodottoId;
    private String nomeProdotto;
    private Double lat;
    private Double lng;
    private String descrizioneFase;
}