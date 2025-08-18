package unicam.progetto_filiera_springboot.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "utenti")
public class Utente {

    @Id
    @Column(length = 50)
    private String username; // PK

    @Column(nullable = false, length = 50)
    private String password;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, length = 50)
    private String cognome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Ruolo ruolo;

    @Column(nullable = false)
    private double fondi = 0.0;

    protected Utente() {
    }

    public Utente(String username, String password, String nome, String cognome, Ruolo ruolo) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
        this.fondi = 0.0;
    }

    // Getter e Setter (puoi aggiungere Lombok se vuoi)
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    public double getFondi() {
        return fondi;
    }

    public void setFondi(double fondi) {
        this.fondi = fondi;
    }
}
