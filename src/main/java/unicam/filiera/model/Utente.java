// -------- Utente.java --------
package unicam.filiera.model;

/**
 * Interfaccia comune a tutti i tipi di utente.
 */
public interface Utente {
    String getUsername();    // per UtenteGenerico puoi restituire null o ""
    String getNome();        // per UtenteGenerico puoi restituire qualcosa di generico
    String getCognome();
}
