package unicam.filiera.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PathDto {
    private Long id;
    private Long prodottoTrasformatoId;
    private List<double[]> coords; // es. [[lat, lng], [lat, lng]]
}
