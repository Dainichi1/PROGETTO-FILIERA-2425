package unicam.filiera.factory;

import unicam.filiera.model.*;

public class UtenteFactory {

    /**
     * Crea un nuovo utente da salvare nel DB.
     */
    public static UtenteAutenticato creaUtente(String username, String password, String nome, String cognome, Ruolo ruolo) {
        return new UtenteAutenticato(username, password, nome, cognome, ruolo);
    }

    /**
     * Crea un attore con comportamento specifico dopo il login.
     */
    public static UtenteAutenticato creaAttore(String username, String password, String nome, String cognome, Ruolo ruolo) {
        return switch (ruolo) {
            case PRODUTTORE -> new Produttore(username, password, nome, cognome);
            case TRASFORMATORE -> new Trasformatore(username, password, nome, cognome);
            case DISTRIBUTORE_TIPICITA -> new DistributoreTipicita(username, password, nome, cognome);
            case CURATORE -> new Curatore(username, password, nome, cognome);
            case ANIMATORE -> new Animatore(username, password, nome, cognome);
            case ACQUIRENTE -> new Acquirente(username, password, nome, cognome);
            case GESTORE_PIATTAFORMA -> new GestorePiattaforma(username, password, nome, cognome);
        };
    }

}
