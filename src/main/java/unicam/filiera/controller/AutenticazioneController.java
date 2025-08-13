package unicam.filiera.controller;

import unicam.filiera.dao.JdbcUtenteDAO;
import unicam.filiera.dao.UtenteDAO;
import unicam.filiera.factory.UtenteFactory;
import unicam.filiera.model.Acquirente;
import unicam.filiera.model.Utente;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Ruolo;

public class AutenticazioneController {

    private final UtenteDAO utenteDAO;

    public AutenticazioneController(UtenteDAO utenteDAO) {
        this.utenteDAO = utenteDAO;
    }

    public AutenticazioneController() {
        this(JdbcUtenteDAO.getInstance());
    }

    public RegistrazioneEsito registrati(
            String username, String password,
            String nome, String cognome,
            Ruolo ruolo) {

        if (utenteDAO.existsUsername(username)) {
            return RegistrazioneEsito.USERNAME_GIA_ESISTENTE;
        }
        if (utenteDAO.existsPersona(nome, cognome)) {
            return RegistrazioneEsito.PERSONA_GIA_REGISTRATA;
        }

        UtenteAutenticato nuovoUtente;
        if (ruolo == Ruolo.ACQUIRENTE) {
            // fondi iniziali a 0.0 (o il valore che preferisci)
            nuovoUtente = new Acquirente(username, password, nome, cognome, 0.0);
        } else {
            nuovoUtente = new UtenteAutenticato(username, password, nome, cognome, ruolo);
        }

        boolean success = utenteDAO.registraUtente(nuovoUtente);
        return success ? RegistrazioneEsito.SUCCESSO : RegistrazioneEsito.USERNAME_GIA_ESISTENTE;
    }

    public Utente login(String username, String password) {
        UtenteAutenticato raw = utenteDAO.login(username, password);
        if (raw == null) return null;

        if (raw.getRuolo() == Ruolo.ACQUIRENTE) {
            // evita ClassCastException se il DAO non ha (ancora) mappato Acquirente
            double fondi = (raw instanceof Acquirente a) ? a.getFondi() : 0.0;
            return UtenteFactory.creaAttore(
                    raw.getUsername(), raw.getPassword(),
                    raw.getNome(), raw.getCognome(),
                    raw.getRuolo(),
                    fondi
            );
        } else {
            return UtenteFactory.creaAttore(
                    raw.getUsername(), raw.getPassword(),
                    raw.getNome(), raw.getCognome(),
                    raw.getRuolo()
            );
        }
    }
}
