package unicam.filiera.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO generico che rappresenta un elemento della piattaforma
 * (utente, prodotto, pacchetto, fiera, visita, acquisto, post social ecc.).
 * Viene usato dal gestore per il riepilogo dei contenuti.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ElementoPiattaformaDto {

    private String id;          // ID o username
    private String nome;        // Nome o descrizione breve
    private String tipo;        // Es. "Utente", "Prodotto", "Pacchetto"
    private String stato;       // Stato (APPROVATO, IN_ATTESA, PUBBLICATO, ecc.)
    private LocalDateTime data; // Data creazione o evento
    private String extra;       // Info aggiuntive (es. Fondi, Prezzo, Organizzatore)
}
