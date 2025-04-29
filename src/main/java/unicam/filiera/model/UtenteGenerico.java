// -------- UtenteGenerico.java --------
package unicam.filiera.model;

/**
 * Utente non registrato che può solo navigare il marketplace e accedere a login/registrazione.
 */
public class UtenteGenerico implements Utente {
    public UtenteGenerico() {
        // Nessun dato necessario
    }

    @Override
    public String getUsername() {
        return "";  // o null, se preferisci
    }

    @Override
    public String getNome() {
        return "Ospite";
    }

    @Override
    public String getCognome() {
        return "";
    }

    @Override
    public String toString() {
        return "UtenteGenerico{accesso limitato - non autenticato}";
    }
}
