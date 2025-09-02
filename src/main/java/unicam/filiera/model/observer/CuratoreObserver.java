package unicam.filiera.model.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import unicam.filiera.model.Prodotto;

@Component
public class CuratoreObserver implements OsservatoreProdotto {

    private static final Logger log = LoggerFactory.getLogger(CuratoreObserver.class);

    @Override
    public void notifica(Prodotto prodotto, String evento) {
        switch (evento) {
            case "NUOVO_PRODOTTO" ->
                    log.info("📦 Nuovo prodotto in attesa: {} (Creato da: {})", prodotto.getNome(), prodotto.getCreatoDa());
            case "APPROVATO" ->
                    log.info("✅ Prodotto approvato: {} (Creato da: {})", prodotto.getNome(), prodotto.getCreatoDa());
            case "RIFIUTATO" ->
                    log.info("❌ Prodotto rifiutato: {} (Creato da: {}, Commento: {})", prodotto.getNome(), prodotto.getCreatoDa(), prodotto.getCommento());
            case "ELIMINATO_PRODOTTO" ->
                    log.info("🗑️ Prodotto eliminato: {} (Creato da: {})", prodotto.getNome(), prodotto.getCreatoDa());
            default ->
                    log.warn("⚠️ Evento sconosciuto '{}' per prodotto: {}", evento, prodotto.getNome());
        }
    }
}
