package unicam.filiera.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IndirizzoDto {
    private Long id;
    private String indirizzo;
    private String tipo;
    private String nome;
}
