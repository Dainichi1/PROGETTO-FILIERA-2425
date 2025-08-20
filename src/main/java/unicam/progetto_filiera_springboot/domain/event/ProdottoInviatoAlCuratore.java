package unicam.progetto_filiera_springboot.domain.event;

public record ProdottoInviatoAlCuratore(Long prodottoId, String creatoDa) implements DomainEvent {
}
