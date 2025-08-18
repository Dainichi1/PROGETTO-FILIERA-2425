package unicam.progetto_filiera_springboot.domain.event.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unicam.progetto_filiera_springboot.domain.event.*;

public class NotificaLogListener implements EventListener<DomainEvent> {
    private static final Logger log = LoggerFactory.getLogger(NotificaLogListener.class);

    @Override
    public void onEvent(DomainEvent event) {
        if (event instanceof UtenteRegistrato u) {
            log.info("Observer: nuovo utente registrato username={}", u.getUsername());
        }
    }
}
