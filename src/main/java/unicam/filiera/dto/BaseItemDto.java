package unicam.filiera.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"certificati", "foto"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseItemDto {

    @EqualsAndHashCode.Include
    @NotNull(message = "⚠ Tipo item obbligatorio")
    private ItemTipo tipo;

    @EqualsAndHashCode.Include
    private Long id;

    private String originalName;

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
    @NotNull(message = "⚠ Inserisci un prezzo")
    @Positive(message = "⚠ Il prezzo deve essere positivo")
    private Double prezzo;

    @EqualsAndHashCode.Include
    @NotNull(message = "⚠ Inserisci la quantità")
    @Min(value = 1, message = "⚠ La quantità deve essere almeno 1")
    private Integer quantita;

    private StatoProdotto stato;

    private String commento;  // opzionale, note/moderazione

    private String creatoDa;

    private List<MultipartFile> certificati;
    private List<MultipartFile> foto;

    private String certificatiCsv;
    private String fotoCsv;
}
