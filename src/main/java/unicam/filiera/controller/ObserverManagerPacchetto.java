package unicam.filiera.controller;

import unicam.filiera.model.observer.OsservatorePacchetto;
import unicam.filiera.model.observer.PacchettoNotifier;

/**
 * Helper per registrare/rimuovere gli osservatori di Pacchetto
 * mantenendo separata la logica dalla vista.
 */
public class ObserverManagerPacchetto {
    /** Registra un osservatore che riceverà notifiche sui pacchetti. */
    public static void registraOsservatore(OsservatorePacchetto o) {
        PacchettoNotifier.getInstance().registraOsservatore(o);
    }

    /** Rimuove un osservatore precedentemente registrato. */
    public static void rimuoviOsservatore(OsservatorePacchetto o) {
        PacchettoNotifier.getInstance().rimuoviOsservatore(o);
    }
}
