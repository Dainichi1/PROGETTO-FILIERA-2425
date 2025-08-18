// src/main/java/unicam/progetto_filiera_springboot/domain/event/EventListener.java
package unicam.progetto_filiera_springboot.domain.event;

public interface EventListener<E extends DomainEvent> {
    void onEvent(E event);
}
