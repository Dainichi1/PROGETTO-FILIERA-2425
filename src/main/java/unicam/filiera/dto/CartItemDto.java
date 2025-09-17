package unicam.filiera.dto;

import lombok.*;

/**
 * DTO che rappresenta un singolo item presente nel carrello.
 * Tiene traccia anche della disponibilità per validare le quantità.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CartItemDto {

    @EqualsAndHashCode.Include
    private ItemTipo tipo;   // usa enum invece di String

    @EqualsAndHashCode.Include
    private Long id;         // id dell’item originale

    private String nome;
    private int quantita;
    private double prezzoUnitario;
    private double totale;

    /**
     * Disponibilità residua dell'item (presa da prodotto/pacchetto/trasformato).
     */
    private int disponibilita;

    /**
     * Setter customizzato: aggiorna il totale e controlla i vincoli.
     */
    public void setQuantita(int quantita) {
        if (quantita <= 0) {
            throw new IllegalArgumentException("⚠ La quantità deve essere maggiore di 0");
        }
        if (disponibilita > 0 && quantita > disponibilita) {
            throw new IllegalArgumentException("⚠ Quantità richiesta superiore alla disponibilità (" + disponibilita + ")");
        }
        this.quantita = quantita;
        this.totale = this.prezzoUnitario * this.quantita;
    }

    /**
     * Utility per aggiornare il totale (es. se cambia il prezzo).
     */
    public void recalculateTotale() {
        this.totale = this.prezzoUnitario * this.quantita;
    }
}
