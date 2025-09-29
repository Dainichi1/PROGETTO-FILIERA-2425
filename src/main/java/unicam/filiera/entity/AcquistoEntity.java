package unicam.filiera.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import unicam.filiera.model.StatoPagamento;
import unicam.filiera.model.TipoMetodoPagamento;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "acquisti")
public class AcquistoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usernameAcquirente;

    @Column(nullable = false)
    private double totale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPagamento statoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMetodoPagamento tipoMetodoPagamento;

    @Column(nullable = false)
    private LocalDateTime dataOra;

    private Double fondiPreAcquisto;
    private Double fondiPostAcquisto;

    @Column(length = 2000)
    private String elencoItem;

    // relazione con gli item
    @OneToMany(mappedBy = "acquisto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcquistoItemEntity> items;
}
