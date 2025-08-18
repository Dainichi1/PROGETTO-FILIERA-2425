package unicam.progetto_filiera_springboot.domain.event;

import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER - Publisher Singleton
 */
public class EventPublisher {
    private static final EventPublisher INSTANCE = new EventPublisher();
    private final List<EventListener<? super DomainEvent>> listeners = new ArrayList<>();

    private EventPublisher() {
    }

    public static EventPublisher getInstance() {
        return INSTANCE;
    }

    public void register(EventListener<? super DomainEvent> l) {
        listeners.add(l);
    }

    public void publish(DomainEvent event) {
        for (EventListener<? super DomainEvent> l : listeners) l.onEvent(event);
    }
}
