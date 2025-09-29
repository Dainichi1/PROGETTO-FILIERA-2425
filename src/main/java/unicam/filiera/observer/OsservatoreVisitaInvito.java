package unicam.filiera.observer;

import unicam.filiera.model.VisitaInvito;

/**
 * Interfaccia per gli osservatori di eventi sulle visite ad invito.
 */
public interface OsservatoreVisitaInvito {
    void notifica(VisitaInvito visita, String evento);
}
