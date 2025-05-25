package unicam.filiera.util;

import unicam.filiera.model.Fiera;
import unicam.filiera.dao.PrenotazioneFieraDAO;

public class ValidatorePrenotazioneFiera {

    // Validazione completa
    public static void validaPrenotazione(
            long idFiera,
            int numeroPersone,
            Fiera fiera,
            String username,
            PrenotazioneFieraDAO prenDAO,
            double fondiAcquirente
    ) {
        validaNumeroPersone(numeroPersone);
        validaFieraNonNull(fiera);
        validaNonPrenotato(prenDAO, idFiera, username);
        validaFondiSufficienti(fiera, numeroPersone, fondiAcquirente);
    }

    public static void validaNumeroPersone(int n) {
        if (n <= 0)
            throw new IllegalArgumentException("Numero persone non valido.");
    }

    public static void validaFieraNonNull(Fiera fiera) {
        if (fiera == null)
            throw new IllegalArgumentException("Fiera non trovata.");
    }

    public static void validaNonPrenotato(PrenotazioneFieraDAO dao, long idFiera, String username) {
        if (dao.existsByFieraAndAcquirente(idFiera, username))
            throw new IllegalArgumentException("Hai già prenotato questa fiera.");
    }

    public static void validaFondiSufficienti(Fiera fiera, int numeroPersone, double fondiAcquirente) {
        double costoTotale = fiera.getPrezzo() * numeroPersone;
        if (fondiAcquirente < costoTotale)
            throw new IllegalArgumentException("Fondi insufficienti per prenotare questa fiera.");
    }
}
