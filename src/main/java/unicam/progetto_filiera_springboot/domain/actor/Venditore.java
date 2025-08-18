package unicam.progetto_filiera_springboot.domain.actor;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public abstract class Venditore extends UtenteAutenticato {
    protected Venditore(String username, String password, String nome, String cognome, Ruolo ruolo) {
        super(username, password, nome, cognome, ruolo);
    }
    public void vendi() {
        System.out.println("[" + getRuolo() + "] puo' vendere prodotti.");
    }
}
