package unicam.filiera.model.observer;

public interface OsservatoreItem {
    /**
     * Chiamato quando la disponibilità di un item (Prodotto/Pacchetto) viene aggiornata.
     *
     * @param nomeItem Nome dell'item aggiornato (puoi aggiungere altri parametri)
     * @param evento   Tipo di evento, es. "AGGIORNAMENTO_DISPONIBILITA"
     */
    void notificaItem(String nomeItem, String evento);
}
