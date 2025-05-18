package unicam.filiera.model.observer;

import unicam.filiera.model.Item;
import java.util.ArrayList;
import java.util.List;

public class ItemNotifier {
    private static ItemNotifier instance;
    private final List<OsservatoreItem> osservatori = new ArrayList<>();

    private ItemNotifier() {}

    public static synchronized ItemNotifier getInstance() {
        if (instance == null) instance = new ItemNotifier();
        return instance;
    }

    public void registraOsservatore(OsservatoreItem o) {
        if (o != null && !osservatori.contains(o)) osservatori.add(o);
    }

    public void rimuoviOsservatore(OsservatoreItem o) {
        osservatori.remove(o);
    }

    // NOTIFICA A TUTTI gli osservatori che un item è cambiato
    public void notificaTutti(String nomeItem, String evento) {
        for (OsservatoreItem o : osservatori)
            o.notificaItem(nomeItem, evento);
    }
}
