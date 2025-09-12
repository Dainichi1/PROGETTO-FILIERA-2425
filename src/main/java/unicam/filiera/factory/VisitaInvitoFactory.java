package unicam.filiera.factory;

import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.model.VisitaInvito;

import java.util.Collections;
import java.util.List;

/**
 * Factory per creare oggetti di dominio VisitaInvito a partire dal DTO.
 */
public final class VisitaInvitoFactory {

    private VisitaInvitoFactory() {}

    public static VisitaInvito creaVisita(VisitaInvitoDto dto, String creatore) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO della visita non può essere null");
        }
        if (creatore == null || creatore.isBlank()) {
            throw new IllegalArgumentException("⚠ Creatore mancante");
        }
        if (dto.getDestinatari() == null || dto.getDestinatari().isEmpty()) {
            throw new IllegalArgumentException("⚠ Devi selezionare almeno un destinatario");
        }

        return new VisitaInvito.Builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .indirizzo(dto.getIndirizzo())
                .dataInizio(dto.getDataInizio())
                .dataFine(dto.getDataFine())
                .creatoDa(creatore)
                .stato(StatoEvento.PUBBLICATA) // sempre pubblicata
                .destinatari(List.copyOf(dto.getDestinatari()))
                .build();
    }
}
