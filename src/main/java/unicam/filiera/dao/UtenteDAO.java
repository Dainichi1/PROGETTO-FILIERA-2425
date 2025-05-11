package unicam.filiera.dao;

import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Ruolo;

import java.util.List;

/**
 * Interfaccia per la gestione della persistenza degli utenti.
 */
public interface UtenteDAO {
    boolean registraUtente(UtenteAutenticato utente);

    UtenteAutenticato login(String username, String password);

    List<UtenteAutenticato> findAll();

    boolean existsUsername(String username);

    boolean existsPersona(String nome, String cognome);

    /**
     * Nuovo: recupera tutti gli utenti che hanno uno dei ruoli indicati
     */
    List<UtenteAutenticato> findByRuoli(List<Ruolo> ruoli);
}
