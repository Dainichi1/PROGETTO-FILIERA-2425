package unicam.progetto_filiera_springboot.dto.auth;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public class LoginDto {
    private String username;
    private String password;
    private Ruolo ruolo;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }
}
