package unicam.progetto_filiera_springboot.factory;

import unicam.progetto_filiera_springboot.domain.actor.*;
import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public final class AttoreFactory {

    private AttoreFactory() {}

    public static Attore crea(Ruolo ruolo) {

        return new AttoreGenerico(ruolo);
    }

    public static UtenteAutenticato from(
            String username, String password, String nome, String cognome, Ruolo ruolo, Double fondiIfAny) {

        return switch (ruolo) {
            case PRODUTTORE -> new Produttore(username, password, nome, cognome);
            case TRASFORMATORE -> new Trasformatore(username, password, nome, cognome);
            case DISTRIBUTORE_TIPICITA -> new DistributoreTipicita(username, password, nome, cognome);
            case CURATORE -> new Curatore(username, password, nome, cognome);
            case ANIMATORE -> new Animatore(username, password, nome, cognome);
            case GESTORE_PIATTAFORMA -> new GestorePiattaforma(username, password, nome, cognome);
            case ACQUIRENTE -> new Acquirente(username, password, nome, cognome,
                    fondiIfAny != null ? fondiIfAny : 0.0);
        };
    }

    public static UtenteAutenticato fromEntity(unicam.progetto_filiera_springboot.domain.model.Utente e) {
        Double fondi = (e.getRuolo() == Ruolo.ACQUIRENTE) ? 0.0 : null;
        return from(e.getUsername(), e.getPassword(), e.getNome(), e.getCognome(), e.getRuolo(), fondi);
    }
}