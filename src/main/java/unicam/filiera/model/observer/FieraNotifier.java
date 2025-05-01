package unicam.filiera.model.observer;

import unicam.filiera.model.Fiera;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe che funge da soggetto osservato nel pattern Observer.
 * Notifica tutti gli osservatori registrati quando una fiera viene creata o pubblicata.
 */
public class FieraNotifier {

    private static FieraNotifier instance;

    private final List<OsservatoreFiera> osservatori = new ArrayList<>();

    private FieraNotifier() { }

    /** Ritorna l’istanza singleton di FieraNotifier */
    public static synchronized FieraNotifier getInstance() {
        if (instance == null) {
            instance = new FieraNotifier();
        }
        return instance;
    }

    /**
     * Registra un nuovo osservatore.
     * @param osservatore l’oggetto che implementa OsservatoreFiera
     */
    public void registraOsservatore(OsservatoreFiera osservatore) {
        if (osservatore != null && !osservatori.contains(osservatore)) {
            osservatori.add(osservatore);
        }
    }

    /**
     * Rimuove un osservatore precedentemente registrato.
     * @param osservatore l’osservatore da rimuovere
     */
    public void rimuoviOsservatore(OsservatoreFiera osservatore) {
        osservatori.remove(osservatore);
    }

    /**
     * Notifica tutti gli osservatori registrati di un evento relativo a una fiera.
     * @param fiera   la fiera che ha generato l’evento
     * @param evento  una stringa che identifica l’azione (es. "NUOVA_FIERA", "FIERA_PUBBLICATA")
     */
    public void notificaTutti(Fiera fiera, String evento) {
        for (OsservatoreFiera o : osservatori) {
            o.notifica(fiera, evento);
        }
    }
}
