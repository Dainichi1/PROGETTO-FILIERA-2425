package unicam.filiera.model.observer;

import unicam.filiera.model.Pacchetto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe che funge da soggetto osservato nel pattern Observer per i pacchetti.
 */
public class PacchettoNotifier {

    private static PacchettoNotifier instance;

    private final Set<OsservatorePacchetto> osservatori = new HashSet<>();

    private PacchettoNotifier() {
    }

    public static PacchettoNotifier getInstance() {
        if (instance == null) {
            instance = new PacchettoNotifier();
        }
        return instance;
    }

    public void registraOsservatore(OsservatorePacchetto osservatore) {
        osservatori.add(osservatore);
    }

    public void notificaTutti(Pacchetto pacchetto, String evento) {
        for (OsservatorePacchetto o : osservatori) {
            o.notifica(pacchetto, evento);
        }
    }

    public void rimuoviOsservatore(OsservatorePacchetto osservatore) {
        osservatori.remove(osservatore);
    }
}
