package unicam.filiera.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "acquisto_items")
public class AcquistoItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_acquisto", nullable = false)
    private AcquistoEntity acquisto;

    @Column(nullable = false)
    private String nomeItem;

    @Column(nullable = false)
    private String tipoItem;

    @Column(nullable = false)
    private int quantita;

    @Column(nullable = false)
    private double prezzoUnitario;

    @Column(nullable = false)
    private double totale;
}
