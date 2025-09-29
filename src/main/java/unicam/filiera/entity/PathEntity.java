package unicam.filiera.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "paths")
public class PathEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // collegato al prodotto trasformato
    @Column(nullable = false)
    private Long prodottoTrasformatoId;

    // memorizzo le coordinate come JSON string
    @Lob
    @Column(nullable = false)
    private String coordsJson;
}
