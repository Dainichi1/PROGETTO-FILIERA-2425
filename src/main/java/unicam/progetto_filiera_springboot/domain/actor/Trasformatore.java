package unicam.progetto_filiera_springboot.domain.actor;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public class Trasformatore extends Venditore {
    public Trasformatore(String username, String password, String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.TRASFORMATORE);
    }
}
