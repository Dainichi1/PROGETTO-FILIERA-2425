package unicam.filiera_agricola_2425.models;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Curatore extends UtenteAutenticato {
    private String nome;
    private String cognome;

    @Column(unique = true)
    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Ruolo ruolo;
}
