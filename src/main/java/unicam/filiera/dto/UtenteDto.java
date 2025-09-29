package unicam.filiera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per rappresentare i dati essenziali di un utente lato view/controller.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtenteDto {

    private String username;
    private String nome;
    private String cognome;
    private String ruolo;  // es. PRODUTTORE, DISTRIBUTORE, TRASFORMATORE, GESTORE, ecc.
}
