package unicam.filiera.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "prenotazioni_fiere")
@Getter
@Setter
public class PrenotazioneFieraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_fiera", nullable = false)
    private Long idFiera;

    @Column(name = "username_acquirente", nullable = false, length = 50)
    private String usernameAcquirente;

    @Column(name = "numero_persone", nullable = false)
    private int numeroPersone;

    @Column(name = "data_prenotazione", nullable = false)
    private LocalDateTime dataPrenotazione = LocalDateTime.now();
}
