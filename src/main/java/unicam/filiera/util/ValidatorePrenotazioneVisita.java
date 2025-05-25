package unicam.filiera.util;

import unicam.filiera.model.VisitaInvito;
import unicam.filiera.dao.PrenotazioneVisitaDAO;

public class ValidatorePrenotazioneVisita {

    // Validazione completa
    public static void validaPrenotazione(
            long idVisita,
            int numeroPersone,
            VisitaInvito visita,
            String usernameVenditore,
            PrenotazioneVisitaDAO prenDAO
    ) {
        validaNumeroPersone(numeroPersone);
        validaVisitaNonNull(visita);
        validaNonPrenotato(prenDAO, idVisita, usernameVenditore);
    }

    public static void validaNumeroPersone(int n) {
        if (n <= 0)
            throw new IllegalArgumentException("Numero persone non valido.");
    }

    public static void validaVisitaNonNull(VisitaInvito visita) {
        if (visita == null)
            throw new IllegalArgumentException("Visita non trovata.");
    }

    public static void validaNonPrenotato(PrenotazioneVisitaDAO dao, long idVisita, String usernameVenditore) {
        if (dao.existsByVisitaAndVenditore(idVisita, usernameVenditore))
            throw new IllegalArgumentException("Hai già prenotato questa visita.");
    }

}
