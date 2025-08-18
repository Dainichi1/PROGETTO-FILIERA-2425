package unicam.progetto_filiera_springboot.domain.actor;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public class Animatore extends UtenteAutenticato {
    public Animatore(String username, String password, String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.ANIMATORE);
    }
}
