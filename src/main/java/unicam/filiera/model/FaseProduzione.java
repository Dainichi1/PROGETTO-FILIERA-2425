package unicam.filiera.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import unicam.filiera.dto.ProdottoTrasformatoDto;

import java.util.Objects;

/**
 * Value Object che rappresenta una fase di produzione
 * di un prodotto trasformato.
 */
@Getter
@ToString
@EqualsAndHashCode
public class FaseProduzione {

    private final String descrizioneFase;
    private final String produttoreUsername; // Username (o id) del produttore
    private final Long prodottoOrigineId;    // ID del prodotto base

    public FaseProduzione(String descrizioneFase, String produttoreUsername, Long prodottoOrigineId) {
        this.descrizioneFase = Objects.requireNonNull(descrizioneFase, "Descrizione fase obbligatoria").trim();
        this.produttoreUsername = Objects.requireNonNull(produttoreUsername, "Produttore obbligatorio").trim();
        this.prodottoOrigineId = Objects.requireNonNull(prodottoOrigineId, "Prodotto origine obbligatorio");
    }

    /* --- Factory Methods --- */
    public static FaseProduzione fromDto(ProdottoTrasformatoDto.FaseProduzioneDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO non può essere null");
        }
        return new FaseProduzione(
                dto.getDescrizioneFase(),
                dto.getProduttoreUsername(),
                dto.getProdottoOrigineId()
        );
    }

    public static FaseProduzione fromCsvString(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new IllegalArgumentException("CSV vuoto o nullo per FaseProduzione");
        }
        String[] parts = csv.split("\\|", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Formato CSV non valido per FaseProduzione: " + csv);
        }
        return new FaseProduzione(parts[0], parts[1], Long.valueOf(parts[2]));
    }

    public static boolean isValidCsv(String csv) {
        if (csv == null || csv.isBlank()) return false;
        String[] parts = csv.split("\\|", -1);
        return parts.length == 3 &&
                !parts[0].isBlank() &&
                !parts[1].isBlank() &&
                !parts[2].isBlank();
    }

    public String toCsvString() {
        return descrizioneFase.strip() + "|" +
                produttoreUsername.strip() + "|" +
                prodottoOrigineId;
    }
}
