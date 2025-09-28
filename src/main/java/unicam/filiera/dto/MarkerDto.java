package unicam.filiera.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MarkerDto {
    private Long id;
    private Double lat;
    private Double lng;
    private String label;
    private String color; // es: "#ff0000"
}
