package unicam.filiera.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEventoDto {

    @EqualsAndHashCode.Include
    @NotNull(message = "⚠ Tipo evento obbligatorio")
    private EventoTipo tipo;   // VISITA | FIERA

    @EqualsAndHashCode.Include
    private Long id;           // per modifica

    @EqualsAndHashCode.Include
    @NotBlank(message = "⚠ Nome obbligatorio")
    private String nome;

    @EqualsAndHashCode.Include
    @NotBlank(message = "⚠ Descrizione obbligatoria")
    private String descrizione;

    @EqualsAndHashCode.Include
    @NotBlank(message = "⚠ Indirizzo obbligatorio")
    private String indirizzo;

    @EqualsAndHashCode.Include
    @NotNull(message = "⚠ Data inizio obbligatoria")
    private LocalDate dataInizio;

    @EqualsAndHashCode.Include
    @NotNull(message = "⚠ Data fine obbligatoria")
    private LocalDate dataFine;

}
