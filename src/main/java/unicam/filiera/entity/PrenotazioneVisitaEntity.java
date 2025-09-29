package unicam.filiera.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "prenotazioni_visite")
@Getter
@Setter
public class PrenotazioneVisitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_visita", nullable = false)
    private Long idVisita;

    @Column(name = "username_venditore", nullable = false, length = 50)
    private String usernameVenditore;

    @Column(name = "numero_persone", nullable = false)
    private int numeroPersone;

    @Column(name = "data_prenotazione", nullable = false)
    private LocalDateTime dataPrenotazione = LocalDateTime.now();
}
