package unicam.filiera.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import unicam.filiera.model.StatoProdotto;

@Setter
@Getter
@Entity
@Table(name = "prodotti")
public class ProdottoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descrizione;

    @Column(nullable = false)
    private String indirizzo;

    /**
     * Regola:
     * - In fase di creazione: validazione nel Service → deve essere > 0
     * - In fase di acquisto: può diventare 0
     */
    @Column(nullable = false)
    @Min(0) // garantisce che non possa mai essere negativo
    private int quantita;

    @Column(nullable = false)
    private double prezzo;

    @Column(nullable = false)
    private String creatoDa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoProdotto stato = StatoProdotto.IN_ATTESA;

    private String commento;

    @Column(length = 2000)
    private String certificati;

    @Column(length = 2000)
    private String foto;
}
