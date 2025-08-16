package unicam.filiera.controller;

import unicam.filiera.model.observer.OsservatoreVisitaInvito;
import unicam.filiera.model.observer.VisitaInvitoNotifier;

/**
 * Helper per registrare/rimuovere gli osservatori di Visita‐Invito.
 */
public class ObserverManagerVisitaInvito {

    public static void registraOsservatore(OsservatoreVisitaInvito o) {
        VisitaInvitoNotifier.getInstance().registraOsservatore(o);
    }

    public static void rimuoviOsservatore(OsservatoreVisitaInvito o) {
        VisitaInvitoNotifier.getInstance().rimuoviOsservatore(o);
    }
}
