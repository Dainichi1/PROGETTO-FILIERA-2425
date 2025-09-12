package unicam.filiera.factory;

import unicam.filiera.dto.FieraDto;
import unicam.filiera.model.Fiera;
import unicam.filiera.model.StatoEvento;

/**
 * Factory per creare oggetti di dominio Fiera a partire dal DTO.
 */
public final class FieraFactory {

    private FieraFactory() {}

    public static Fiera creaFiera(FieraDto dto, String creatore) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO della fiera non può essere null");
        }
        if (creatore == null || creatore.isBlank()) {
            throw new IllegalArgumentException("⚠ Creatore mancante");
        }
        if (dto.getPrezzo() < 0) {
            throw new IllegalArgumentException("⚠ Il prezzo non può essere negativo");
        }

        return new Fiera.Builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .indirizzo(dto.getIndirizzo())
                .dataInizio(dto.getDataInizio())
                .dataFine(dto.getDataFine())
                .creatoDa(creatore)
                .stato(StatoEvento.PUBBLICATA) // sempre pubblicata
                .prezzo(dto.getPrezzo())
                .build();
    }
}
