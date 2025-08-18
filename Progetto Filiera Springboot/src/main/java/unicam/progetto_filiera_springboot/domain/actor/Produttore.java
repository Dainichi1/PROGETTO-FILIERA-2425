package unicam.progetto_filiera_springboot.domain.actor;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public class Produttore extends Venditore {
    public Produttore(String username, String password, String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.PRODUTTORE);
    }
}
