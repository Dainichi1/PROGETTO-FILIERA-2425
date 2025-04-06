package unicam.filiera_agricola_2425.models;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UtenteAutenticato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cognome;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Ruolo ruolo;

    // TEMPLATE METHOD
    public String messaggioDashboard() {
        return "Benvenuto " + nome + " (" + ruolo + ") — " + messaggioSpecifico();
    }

    protected abstract String messaggioSpecifico(); // Hook method (verrà implementato nelle sottoclassi)




// Getters e Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }

    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public Ruolo getRuolo() { return ruolo; }

    public void setRuolo(Ruolo ruolo) { this.ruolo = ruolo; }
}
