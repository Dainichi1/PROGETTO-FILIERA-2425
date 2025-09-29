package unicam.filiera.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {

    /**
     * Registra un nuovo emitter per un utente (subscribe).
     */
    SseEmitter addEmitter(String username);

    /**
     * Invia una notifica a un utente specifico.
     */
    void notifyUser(String username, String event, String message);
}
