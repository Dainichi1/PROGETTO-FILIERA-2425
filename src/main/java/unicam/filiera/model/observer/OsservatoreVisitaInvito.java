package unicam.filiera.model.observer;

import unicam.filiera.model.VisitaInvito;

public interface OsservatoreVisitaInvito {
    void notifica(VisitaInvito v, String evento);
}
