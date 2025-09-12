package unicam.filiera.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import unicam.filiera.model.StatoEvento;

import java.time.LocalDate;

/**
 * Entity JPA per le visite ad invito.
 * Mappa la tabella "visite_invito".
 */
@Setter
@Getter
@Entity
@Table(name = "visite_invito")
public class VisitaInvitoEntity {

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

    /**
     * Lista destinatari serializzata come CSV.
     * Esempio: "produttore1,trasformatore2,distributore3"
     */
    @Column(length = 2000, nullable = false)
    private String destinatari;
}
