package unicam.progetto_filiera_springboot.domain.actor;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public class DistributoreTipicita extends Venditore {
    public DistributoreTipicita(String username, String password, String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.DISTRIBUTORE_TIPICITA);
    }
}
