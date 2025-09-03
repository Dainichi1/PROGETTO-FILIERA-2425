package unicam.filiera.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotBlank(message = "⚠ Nome obbligatorio")
    private String nome;

    @NotBlank(message = "⚠ Descrizione obbligatoria")
    private String descrizione;

    @NotBlank(message = "⚠ Indirizzo obbligatorio")
    private String indirizzo;

    @Min(value = 1, message = "⚠ La quantità deve essere almeno 1")
    private int quantita;

    @Positive(message = "⚠ Il prezzo deve essere positivo")
    private double prezzo;

    private String creatoDa;

    @Enumerated(EnumType.STRING)
    private StatoProdotto stato = StatoProdotto.IN_ATTESA;

    private String commento;

    // Per semplicità: CSV di nomi file
    @Column(length = 2000)
    private String certificati;

    @Column(length = 2000)
    private String foto;
}
