package unicam.filiera.util;

import unicam.filiera.dao.FieraDAO;
import unicam.filiera.dao.VisitaInvitoDAO;
import unicam.filiera.dto.AnnuncioEventoDto;
import unicam.filiera.model.Fiera;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.model.VisitaInvito;

public final class ValidatoreAnnuncioEvento {

    private ValidatoreAnnuncioEvento() {}

    /** Validazioni di base sui campi del form */
    public static void validaCampiBase(AnnuncioEventoDto dto) {
        if (dto == null) throw new IllegalArgumentException("Annuncio mancante.");
        if (isBlank(dto.getTitolo())) throw new IllegalArgumentException("Il titolo è obbligatorio.");
        if (dto.getTitolo().length() > 100) throw new IllegalArgumentException("Il titolo non può superare 100 caratteri.");
        if (isBlank(dto.getTesto())) throw new IllegalArgumentException("Il testo è obbligatorio.");
        if (dto.getTesto().length() > 1000) throw new IllegalArgumentException("Il testo non può superare 1000 caratteri.");
        if (dto.getEventoId() <= 0) throw new IllegalArgumentException("Evento non selezionato.");
        if (!"FIERA".equalsIgnoreCase(dto.getTipoEvento()) && !"VISITA".equalsIgnoreCase(dto.getTipoEvento())) {
            throw new IllegalArgumentException("Tipo evento non valido (attesi: FIERA o VISITA).");
        }
    }

    /** Coerenza con il DB: esistenza, proprietario, stato PUBBLICATA */
    public static void validaCoerenzaEvento(
            AnnuncioEventoDto dto,
            String organizzatore,
            FieraDAO fieraDAO,
            VisitaInvitoDAO visitaDAO
    ) {
        if ("FIERA".equalsIgnoreCase(dto.getTipoEvento())) {
            Fiera f = fieraDAO.findById(dto.getEventoId());
            if (f == null) throw new IllegalArgumentException("Fiera non trovata.");
            if (!organizzatore.equals(f.getOrganizzatore()))
                throw new IllegalArgumentException("Non sei l'organizzatore della fiera selezionata.");
            if (f.getStato() != StatoEvento.PUBBLICATA)
                throw new IllegalArgumentException("La fiera non è pubblicata sul Marketplace.");
        } else {
            VisitaInvito v = visitaDAO.findById(dto.getEventoId());
            if (v == null) throw new IllegalArgumentException("Visita su invito non trovata.");
            if (!organizzatore.equals(v.getOrganizzatore()))
                throw new IllegalArgumentException("Non sei l'organizzatore della visita selezionata.");
            if (v.getStato() != StatoEvento.PUBBLICATA)
                throw new IllegalArgumentException("La visita non è pubblicata sul Marketplace.");
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
