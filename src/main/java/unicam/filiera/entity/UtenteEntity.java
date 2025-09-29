package unicam.filiera.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import unicam.filiera.model.Ruolo;

@Setter
@Getter
@Entity
@Table(name = "utenti")
public class UtenteEntity {

    @Id
    @NotBlank(message = "⚠ Username obbligatorio")
    private String username;

    @NotBlank(message = "⚠ Password obbligatoria")
    private String password;

    @NotBlank(message = "⚠ Nome obbligatorio")
    private String nome;

    @NotBlank(message = "⚠ Cognome obbligatorio")
    private String cognome;

    @NotNull(message = "⚠ Devi selezionare un ruolo")
    @Enumerated(EnumType.STRING)
    private Ruolo ruolo;

    private Double fondi = 0.0;

}
