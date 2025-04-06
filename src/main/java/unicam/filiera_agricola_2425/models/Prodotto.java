package unicam.filiera_agricola_2425.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Prodotto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private StatoProdotto stato = StatoProdotto.IN_BOZZA;


    private String nome;
    private double prezzo;
    private int quantita;
    private String descrizione;
    @OneToMany(mappedBy = "prodotto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImmagineProdotto> immagini;

    @OneToMany(mappedBy = "prodotto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CertificatoProdotto> certificazioni;


    @ManyToOne
    private Produttore produttore;

    public enum StatoProdotto {
        IN_BOZZA,
        IN_ATTESA_APPROVAZIONE,
        APPROVATO,
        RIFIUTATO
    }

}
