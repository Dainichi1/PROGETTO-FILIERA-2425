package unicam.filiera.controller;

import unicam.filiera.model.observer.FieraNotifier;
import unicam.filiera.model.observer.OsservatoreFiera;

/**
 * Gestisce l’iscrizione/rimozione degli osservatori di Fiera
 */
public class ObserverManagerFiera {
    public static void registraOsservatore(OsservatoreFiera o) {
        FieraNotifier.getInstance().registraOsservatore(o);
    }

    public static void rimuoviOsservatore(OsservatoreFiera o) {
        FieraNotifier.getInstance().rimuoviOsservatore(o);
    }
}
