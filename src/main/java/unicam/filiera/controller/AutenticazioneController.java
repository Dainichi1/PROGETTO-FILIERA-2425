package unicam.filiera.controller;

import unicam.filiera.dao.JdbcUtenteDAO;
import unicam.filiera.dao.UtenteDAO;
import unicam.filiera.factory.UtenteFactory;
import unicam.filiera.model.Utente;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Ruolo;

public class AutenticazioneController {

    private final UtenteDAO utenteDAO;

    /**
     * Iniezione di dipendenza per facilitare i test
     */
    public AutenticazioneController(UtenteDAO utenteDAO) {
        this.utenteDAO = utenteDAO;
    }

    /**
     * Costruttore di convenienza per l'app reale
     */
    public AutenticazioneController() {
        this(JdbcUtenteDAO.getInstance());
    }

    /**
     * Registra un nuovo utente autenticato nel database.
     * Usa la factory per creare l'oggetto UtenteAutenticato.
     */
    public RegistrazioneEsito registrati(
            String username, String password,
            String nome, String cognome,
            Ruolo ruolo) {
        // Controllo: username già esistente
        if (utenteDAO.existsUsername(username)) {
            return RegistrazioneEsito.USERNAME_GIA_ESISTENTE;
        }

        // Controllo: persona già registrata
        if (utenteDAO.existsPersona(nome, cognome)) {
            return RegistrazioneEsito.PERSONA_GIA_REGISTRATA;
        }

        // Crea l'utente da registrare
        UtenteAutenticato nuovoUtente = (UtenteAutenticato) UtenteFactory.creaUtenteRegistrazione(
                username, password, nome, cognome, ruolo);

        boolean success = utenteDAO.registraUtente(nuovoUtente);
        return success ? RegistrazioneEsito.SUCCESSO : RegistrazioneEsito.USERNAME_GIA_ESISTENTE;
    }

    /**
     * Esegue il login e restituisce l'attore corretto (sottotipo di UtenteAutenticato).
     * Ritorna null se le credenziali non sono valide.
     */
    public Utente login(String username, String password) {
        // login grezzo dal DAO
        UtenteAutenticato raw = utenteDAO.login(username, password);
        if (raw == null) {
            return null;
        }

        // Usa la factory per ottenere il sottotipo corretto
        return UtenteFactory.creaAttore(
                raw.getUsername(), raw.getPassword(),
                raw.getNome(), raw.getCognome(),
                raw.getRuolo());
    }
}