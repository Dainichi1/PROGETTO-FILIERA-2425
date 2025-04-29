package unicam.filiera.model;

public enum Ruolo {
    PRODUTTORE(true),
    TRASFORMATORE(true),
    DISTRIBUTORE_TIPICITA(true),
    CURATORE(true),
    ANIMATORE(true),
    GESTORE_PIATTAFORMA(true),
    ACQUIRENTE(true);

    private final boolean visibile;
    Ruolo(boolean visibile) { this.visibile = visibile; }
    public boolean isVisibile() { return visibile; }
}