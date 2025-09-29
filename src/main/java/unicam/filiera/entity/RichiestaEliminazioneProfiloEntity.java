package unicam.filiera.entity;

import jakarta.persistence.*;
import lombok.*;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;

import java.time.LocalDateTime;

@Entity
@Table(name = "richieste_eliminazione_profilo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RichiestaEliminazioneProfiloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // uso Long come nelle altre entity

    @Column(nullable = false, length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoRichiestaEliminazioneProfilo stato;

    @Column(name = "data_richiesta", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dataRichiesta;

    @PrePersist
    void onCreate() {
        if (dataRichiesta == null) {
            dataRichiesta = LocalDateTime.now();
        }
    }
}
