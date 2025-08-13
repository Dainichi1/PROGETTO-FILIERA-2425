package unicam.filiera.model.observer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EliminazioneProfiloNotifier {

    private static final EliminazioneProfiloNotifier INSTANCE = new EliminazioneProfiloNotifier();
    public static EliminazioneProfiloNotifier getInstance() { return INSTANCE; }

    // mappa username -> observers interessati a quell’utente
    private final Map<String, List<OsservatoreEliminazioneProfilo>> observers = new HashMap<>();

    public synchronized void subscribe(String username, OsservatoreEliminazioneProfilo obs) {
        observers.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(obs);
    }

    public synchronized void unsubscribe(String username, OsservatoreEliminazioneProfilo obs) {
        List<OsservatoreEliminazioneProfilo> list = observers.get(username);
        if (list != null) list.remove(obs);
    }

    public void notificaRifiutata(String username, int richiestaId, String motivo) {
        List<OsservatoreEliminazioneProfilo> list = observers.get(username);
        if (list == null) return;
        for (OsservatoreEliminazioneProfilo o : list) {
            o.onRichiestaRifiutata(username, richiestaId, motivo);
        }
    }

    public void notificaEliminata(String username, int richiestaId) {
        List<OsservatoreEliminazioneProfilo> list = observers.get(username);
        if (list == null) return;
        for (OsservatoreEliminazioneProfilo o : list) {
            o.onProfiloEliminato(username, richiestaId);
        }
    }
}
