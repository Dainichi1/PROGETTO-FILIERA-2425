// -------- UtenteAutenticato.java --------
package unicam.filiera.model;

/**
 * Super‐classe per tutti gli utenti autenticati con un ruolo.
 */
public class UtenteAutenticato implements Utente {
    private String username;
    private String password;
    private String nome;
    private String cognome;
    private Ruolo ruolo;

    public UtenteAutenticato(String username, String password, String nome, String cognome, Ruolo ruolo) {
        this.username = username;
        this.password = password;
        this.nome     = nome;
        this.cognome  = cognome;
        this.ruolo    = ruolo;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public String getCognome() {
        return cognome;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

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
