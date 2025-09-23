package unicam.filiera.model;

import lombok.*;

/**
 * DTO di supporto per rappresentare i criteri di ricerca/ordinamento
 * applicabili ai contenuti della piattaforma.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CriteriRicerca {

    private String testo;          // ricerca libera (es. nome, descrizione)
    private String stato;          // stato del contenuto (es. APPROVATO, RIFIUTATO, IN_ATTESA)
    private String ordinamento;    // campo su cui ordinare (es. "nome", "data", "prezzo")
    private boolean crescente;     // true = ascendente, false = discendente
}
