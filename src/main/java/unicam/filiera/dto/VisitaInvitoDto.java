package unicam.filiera.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import unicam.filiera.model.StatoEvento;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO per la creazione o modifica di una Visita ad invito da parte dell’Animatore.
 * Estende BaseEventoDto.
 *
 * I destinatari ora contengono gli **username** degli utenti invitati
 * (produttori, trasformatori, distributori_tipicita).
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VisitaInvitoDto extends BaseEventoDto {

    @NotEmpty(message = "⚠ Devi selezionare almeno un destinatario")
    private List<String> destinatari = new ArrayList<>(); // lista di username

    private String creatoDa;
    private StatoEvento stato;
}
