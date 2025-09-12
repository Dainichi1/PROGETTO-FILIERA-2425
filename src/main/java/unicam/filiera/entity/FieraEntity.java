package unicam.filiera.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import unicam.filiera.model.StatoEvento;

import java.time.LocalDate;

/**
 * Entity JPA per le fiere.
 * Mappa la tabella "fiere".
 */
@Setter
@Getter
@Entity
@Table(name = "fiere")
public class FieraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 2000)
    private String descrizione;

    @Column(nullable = false)
    private String indirizzo;

    @Column(nullable = false)
    private LocalDate dataInizio;

    @Column(nullable = false)
    private LocalDate dataFine;

    @Column(nullable = false)
    private String creatoDa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoEvento stato = StatoEvento.PUBBLICATA;

    @Column(nullable = false)
    private double prezzo;
}
