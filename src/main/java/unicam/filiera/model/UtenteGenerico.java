package unicam.filiera.model;

/**
 * Utente non registrato che può solo navigare il marketplace e accedere a login/registrazione.
 */
public class UtenteGenerico {

    public UtenteGenerico() {
        // Nessun dato necessario
    }

    @Override
    public String toString() {
        return "UtenteGenerico{accesso limitato - non autenticato}";
    }
}
