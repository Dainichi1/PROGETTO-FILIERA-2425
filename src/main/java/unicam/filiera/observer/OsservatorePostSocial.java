package unicam.filiera.observer;

import unicam.filiera.model.PostSocial;

/**
 * Interfaccia per gli osservatori di eventi sui post social.
 */
public interface OsservatorePostSocial {
    void notifica(PostSocial post, String evento);
}
