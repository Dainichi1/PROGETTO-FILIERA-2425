package unicam.filiera.model.observer;

import unicam.filiera.model.VisitaInvito;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VisitaInvitoNotifier {
    private static VisitaInvitoNotifier instance;
    private final List<OsservatoreVisitaInvito> osservatori
            = new CopyOnWriteArrayList<>();

    private VisitaInvitoNotifier() {
    }

    public static synchronized VisitaInvitoNotifier getInstance() {
        if (instance == null) instance = new VisitaInvitoNotifier();
        return instance;
    }

    public void registraOsservatore(OsservatoreVisitaInvito o) {
        if (o != null && !osservatori.contains(o)) osservatori.add(o);
    }

    public void rimuoviOsservatore(OsservatoreVisitaInvito o) {
        osservatori.remove(o);
    }

    public void notificaTutti(VisitaInvito v, String evento) {
        for (var o : osservatori) o.notifica(v, evento);
    }
}