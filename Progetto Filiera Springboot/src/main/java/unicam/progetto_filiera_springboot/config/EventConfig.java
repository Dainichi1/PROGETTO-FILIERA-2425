package unicam.progetto_filiera_springboot.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import unicam.progetto_filiera_springboot.domain.event.EventPublisher;
import unicam.progetto_filiera_springboot.domain.event.listeners.NotificaLogListener;

@Configuration
public class EventConfig {
    @PostConstruct
    public void registerListeners() {
        EventPublisher.getInstance().register(new NotificaLogListener());
    }
}
