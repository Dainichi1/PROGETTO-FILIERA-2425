package unicam.filiera.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import unicam.filiera.model.StatoProdotto;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "prodotti_trasformati")
public class ProdottoTrasformatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descrizione;

    @Column(nullable = false)
    private String indirizzo;

    @Column(nullable = false)
    @Min(0) // garantisce che non possa mai essere negativa
    private int quantita;

    @Column(nullable = false)
    private double prezzo;

    @Column(nullable = false)
    private String creatoDa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoProdotto stato = StatoProdotto.IN_ATTESA;

    private String commento;

    // Relazione embedded invece del CSV
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "fasi_produzione",
            joinColumns = @JoinColumn(name = "prodotto_trasformato_id")
    )
    @Size(min = 2, message = "Un prodotto trasformato deve contenere almeno 2 fasi di produzione")
    private List<FaseProduzioneEmbeddable> fasiProduzione = new ArrayList<>();

    // CSV dei nomi file
    @Column(length = 2000)
    private String certificati;

    @Column(length = 2000)
    private String foto;
}
