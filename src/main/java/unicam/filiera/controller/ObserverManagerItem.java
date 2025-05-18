package unicam.filiera.controller;

import unicam.filiera.model.observer.ItemNotifier;
import unicam.filiera.model.observer.OsservatoreItem;

public class ObserverManagerItem {
    public static void registraOsservatore(OsservatoreItem o) {
        ItemNotifier.getInstance().registraOsservatore(o);
    }
    public static void rimuoviOsservatore(OsservatoreItem o) {
        ItemNotifier.getInstance().rimuoviOsservatore(o);
    }
    public static void notificaAggiornamento(String nomeItem, String evento) {
        ItemNotifier.getInstance().notificaTutti(nomeItem, evento);
    }
}
