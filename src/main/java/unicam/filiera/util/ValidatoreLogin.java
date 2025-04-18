package unicam.filiera.util;


public class ValidatoreLogin {

    public static void valida(String username, String password) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("⚠ Inserisci uno username.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("⚠ Inserisci una password.");
    }
}
