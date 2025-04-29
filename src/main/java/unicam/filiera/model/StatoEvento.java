package unicam.filiera.model;

/**
 * Stati possibili di un evento/fiera.
 */
public enum StatoEvento {
    IN_PREPARAZIONE,   // creato ma non ancora pubblicato
    ATTIVA,            // visibile e prenotabile nel marketplace
    CANCELLATA,        // annullata dall’animatore o per cause di forza maggiore
    CONCLUSA           // dataFine superata
}
