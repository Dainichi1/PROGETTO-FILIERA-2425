package unicam.filiera_agricola_2425.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Prodotto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private double prezzo;
    private int quantita;
    private String descrizione;
    private String certificazione;
    private String immagine; // URL o path

    @ManyToOne
    private Produttore produttore;
}
