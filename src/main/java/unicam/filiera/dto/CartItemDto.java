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
    private ItemTipo tipo;

    @EqualsAndHashCode.Include
    private Long id;

    private String nome;
    private int quantita;
    private double prezzoUnitario;
    private double totale;

    /**
     * Disponibilità residua per l’utente (calcolata come magazzino - quantità in carrello).
     */
    private int disponibilita;

    /**
     * Quantità totale reale a magazzino (serve per ricalcoli).
     */
    private int disponibilitaMagazzino;

    public void setQuantita(int quantita) {
        this.quantita = quantita;
        this.totale = this.prezzoUnitario * this.quantita;
    }

    public void recalculateTotale() {
        this.totale = this.prezzoUnitario * this.quantita;
    }
}
