package unicam.progetto_filiera_springboot.domain.actor;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public class GestorePiattaforma extends UtenteAutenticato {
    public GestorePiattaforma(String username, String password, String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.GESTORE_PIATTAFORMA);
    }
}
