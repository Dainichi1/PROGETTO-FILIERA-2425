package unicam.filiera.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GeocodedIndirizzoDto {
    private Long id;
    private String indirizzo;
    private String tipo;
    private String nome;
    private Double lat;
    private Double lng;
}
