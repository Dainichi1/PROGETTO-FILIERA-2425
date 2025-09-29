package unicam.filiera.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdottoTrasformatoPathDto {
    private Long trasformatoId;
    private String nomeTrasformato;
    private Double latTrasformato;
    private Double lngTrasformato;

    private List<FaseProdottoDto> fasi;
}