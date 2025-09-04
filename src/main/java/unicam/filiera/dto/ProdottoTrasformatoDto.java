package unicam.filiera.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO per la creazione o modifica di un Prodotto Trasformato da parte del Trasformatore.
 * Include le fasi di produzione che collegano produttori e prodotti base approvati.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProdottoTrasformatoDto {

    private String originalName;

    @NotBlank(message = "⚠ Nome prodotto obbligatorio")
    private String nome;

    @NotBlank(message = "⚠ Descrizione obbligatoria")
    private String descrizione;

    @NotNull(message = "⚠ Inserisci la quantità")
    @Min(value = 1, message = "⚠ La quantità deve essere almeno 1")
    private Integer quantita;

    @NotNull(message = "⚠ Inserisci un prezzo")
    @Positive(message = "⚠ Il prezzo deve essere positivo")
    private Double prezzo;

    @NotBlank(message = "⚠ Indirizzo obbligatorio")
    private String indirizzo;

    // File caricati (validazione gestita nei Controller)
    private List<MultipartFile> certificati;
    private List<MultipartFile> foto;

    /**
     * Elenco delle fasi di produzione del prodotto trasformato.
     * Devono esserci almeno 2 fasi e i prodotti devono essere approvati.
     */
    @Size(min = 2, message = "⚠ Devi inserire almeno 2 fasi di produzione")
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
