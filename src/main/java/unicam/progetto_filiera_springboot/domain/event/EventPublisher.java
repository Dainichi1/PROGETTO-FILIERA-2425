package unicam.progetto_filiera_springboot.domain.event;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    public void register(EventListener listener) {
        listeners.add(listener);
    }

    public void publish(DomainEvent event) {
        for (EventListener l : listeners) {
            l.onEvent(event);
        }
    }
}
