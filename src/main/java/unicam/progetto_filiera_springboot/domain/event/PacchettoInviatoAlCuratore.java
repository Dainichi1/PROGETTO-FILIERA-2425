package unicam.progetto_filiera_springboot.domain.event;

/**
 * Evento dominio: un pacchetto è stato inviato dal distributore al Curatore
 * per la revisione e l’approvazione.
 */
public record PacchettoInviatoAlCuratore(Long pacchettoId, String creatoDa) implements DomainEvent {
}
