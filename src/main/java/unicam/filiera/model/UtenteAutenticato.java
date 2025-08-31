// -------- UtenteAutenticato.java --------
package unicam.filiera.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Super‐classe per tutti gli utenti autenticati con un ruolo.
 */
@Getter
public class UtenteAutenticato implements Utente {
    private String username;
    private String password;
    @Setter
    private String nome;
    @Setter
    private String cognome;
    @Setter
    private Ruolo ruolo;


    public UtenteAutenticato(String username, String password, String nome, String cognome, Ruolo ruolo) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
    }

    // Metodi getter e setter (senza fondi)

    @Override
    public String toString() {
        return "UtenteAutenticato{" +
                "username='" + username + '\'' +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", ruolo=" + ruolo +
                '}';
    }
}
