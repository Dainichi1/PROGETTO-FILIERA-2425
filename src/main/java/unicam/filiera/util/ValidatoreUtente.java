package unicam.filiera.util;

public class ValidatoreUtente {

    /**
     * Valida i dati necessari alla registrazione di un nuovo utente.
     *
     * @throws IllegalArgumentException se un campo è mancante o vuoto.
     */
    public static void validaRegistrazione(String username, String password, String nome, String cognome) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("⚠ Username mancante o vuoto");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("⚠ Password mancante o vuota");

        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("⚠ Nome mancante o vuoto");

        if (cognome == null || cognome.isBlank())
            throw new IllegalArgumentException("⚠ Cognome mancante o vuoto");
    }
}
