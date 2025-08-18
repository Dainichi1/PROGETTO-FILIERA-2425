package unicam.progetto_filiera_springboot.domain.actor;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public class UtenteAutenticato implements Utente {
    private final String username;
    private final String password;
    private String nome;
    private String cognome;
    private Ruolo ruolo;

    public UtenteAutenticato(String username, String password, String nome, String cognome, Ruolo ruolo) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
    }

    @Override public String getUsername() { return username; }
    public String getPassword() { return password; }
    @Override public String getNome() { return nome; }
    @Override public String getCognome() { return cognome; }
    public Ruolo getRuolo() { return ruolo; }

    public void setNome(String nome) { this.nome = nome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public void setRuolo(Ruolo ruolo) { this.ruolo = ruolo; }

    @Override public String toString() {
        return "UtenteAutenticato{username='%s', nome='%s', cognome='%s', ruolo=%s}"
                .formatted(username, nome, cognome, ruolo);
    }
}
