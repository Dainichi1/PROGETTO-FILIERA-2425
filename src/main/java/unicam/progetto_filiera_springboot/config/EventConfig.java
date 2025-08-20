// config/EventConfig.java
package unicam.progetto_filiera_springboot.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import unicam.progetto_filiera_springboot.domain.event.EventPublisher;
import unicam.progetto_filiera_springboot.domain.event.listeners.NotificaLogListener;

@Configuration
@RequiredArgsConstructor
public class EventConfig {

    private final EventPublisher publisher;

    @PostConstruct
    void registerListeners() {
        publisher.register(new NotificaLogListener());
    }
}
