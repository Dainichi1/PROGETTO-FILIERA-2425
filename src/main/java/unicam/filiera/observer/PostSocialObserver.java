package unicam.filiera.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import unicam.filiera.model.PostSocial;

/**
 * Observer per monitorare la pubblicazione dei post social.
 */
@Component
public class PostSocialObserver implements OsservatorePostSocial {

    private static final Logger log = LoggerFactory.getLogger(PostSocialObserver.class);

    @Override
    public void notifica(PostSocial post, String evento) {
        switch (evento) {
            case "NUOVO_POST" ->
                    log.info("📝 Nuovo post pubblicato da {} su {} ({}): {}",
                            post.getAutoreUsername(),
                            post.getNomeItem(),
                            post.getTipoItem(),
                            post.getTitolo());

            case "ELIMINATO_POST" ->
                    log.info("🗑️ Post eliminato da {}: {}",
                            post.getAutoreUsername(),
                            post.getTitolo());

            default ->
                    log.warn("⚠️ Evento sconosciuto '{}' per post di {}", evento, post.getAutoreUsername());
        }
    }
}
