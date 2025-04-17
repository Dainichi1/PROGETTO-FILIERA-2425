package unicam.filiera.controller;

import unicam.filiera.dao.UtenteDAO;
import unicam.filiera.factory.UtenteFactory;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Ruolo;

public class AutenticazioneController {

    private final UtenteDAO utenteDAO;

    public AutenticazioneController() {
        this.utenteDAO = UtenteDAO.getInstance();
    }

    /**
     * Registra un nuovo utente autenticato.
     *
     * @return esito dettagliato della registrazione
     */
    public RegistrazioneEsito registrati(String username, String password, String nome, String cognome, Ruolo ruolo) {
        // Controllo: username già esistente
        if (utenteDAO.esisteUsername(username)) {
            return RegistrazioneEsito.USERNAME_GIA_ESISTENTE;
        }

        // Controllo: persona già registrata
        if (utenteDAO.esistePersona(nome, cognome)) {
            return RegistrazioneEsito.PERSONA_GIA_REGISTRATA;
        }

        UtenteAutenticato nuovoUtente = UtenteFactory.creaUtente(username, password, nome, cognome, ruolo);
        boolean success = utenteDAO.registraUtente(nuovoUtente);
        return success ? RegistrazioneEsito.SUCCESSO : RegistrazioneEsito.USERNAME_GIA_ESISTENTE;
    }

    /**
     * Esegue il login
     */
    public UtenteAutenticato login(String username, String password) {
        return utenteDAO.login(username, password);
    }
}
