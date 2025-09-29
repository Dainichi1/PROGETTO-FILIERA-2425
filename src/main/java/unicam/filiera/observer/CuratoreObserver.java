package unicam.filiera.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.ProdottoTrasformato;

/**
 * Observer unico del Curatore:
 * riceve notifiche su Prodotti, Pacchetti e Prodotti Trasformati.
 */
@Component
public class CuratoreObserver implements OsservatoreProdotto,
        OsservatorePacchetto,
        OsservatoreProdottoTrasformato {

    private static final Logger log = LoggerFactory.getLogger(CuratoreObserver.class);

    /* =======================
       Notifiche sui Prodotti
    ======================= */
    @Override
    public void notifica(Prodotto prodotto, String evento) {
        switch (evento) {
            case "NUOVO_PRODOTTO" ->
                    log.info("📦 Nuovo prodotto in attesa: {} (Creato da: {})",
                            prodotto.getNome(), prodotto.getCreatoDa());
            case "APPROVATO" ->
                    log.info("✅ Prodotto approvato: {} (Creato da: {})",
                            prodotto.getNome(), prodotto.getCreatoDa());
            case "RIFIUTATO" ->
                    log.info("❌ Prodotto rifiutato: {} (Creato da: {}, Commento: {})",
                            prodotto.getNome(), prodotto.getCreatoDa(), prodotto.getCommento());
            case "ELIMINATO_PRODOTTO" ->
                    log.info("🗑️ Prodotto eliminato: {} (Creato da: {})",
                            prodotto.getNome(), prodotto.getCreatoDa());
            default ->
                    log.warn("⚠️ Evento sconosciuto '{}' per prodotto: {}", evento, prodotto.getNome());
        }
    }

    /* =======================
       Notifiche sui Pacchetti
    ======================= */
    @Override
    public void notifica(Pacchetto pacchetto, String evento) {
        switch (evento) {
            case "NUOVO_PACCHETTO" ->
                    log.info("📦 Nuovo pacchetto in attesa: {} (Creato da: {})",
                            pacchetto.getNome(), pacchetto.getCreatoDa());
            case "APPROVATO" ->
                    log.info("✅ Pacchetto approvato: {} (Creato da: {})",
                            pacchetto.getNome(), pacchetto.getCreatoDa());
            case "RIFIUTATO" ->
                    log.info("❌ Pacchetto rifiutato: {} (Creato da: {}, Commento: {})",
                            pacchetto.getNome(), pacchetto.getCreatoDa(), pacchetto.getCommento());
            case "ELIMINATO_PACCHETTO" ->
                    log.info("🗑️ Pacchetto eliminato: {} (Creato da: {})",
                            pacchetto.getNome(), pacchetto.getCreatoDa());
            default ->
                    log.warn("⚠️ Evento sconosciuto '{}' per pacchetto: {}", evento, pacchetto.getNome());
        }
    }

    /* ================================
       Notifiche sui Prodotti Trasformati
    ================================ */
    @Override
    public void notifica(ProdottoTrasformato prodottoTrasformato, String evento) {
        switch (evento) {
            case "NUOVO_PRODOTTO_TRASFORMATO" ->
                    log.info("🍷 Nuovo prodotto trasformato in attesa: {} (Creato da: {})",
                            prodottoTrasformato.getNome(), prodottoTrasformato.getCreatoDa());
            case "APPROVATO" ->
                    log.info("✅ Prodotto trasformato approvato: {} (Creato da: {})",
                            prodottoTrasformato.getNome(), prodottoTrasformato.getCreatoDa());
            case "RIFIUTATO" ->
                    log.info("❌ Prodotto trasformato rifiutato: {} (Creato da: {}, Commento: {})",
                            prodottoTrasformato.getNome(), prodottoTrasformato.getCreatoDa(), prodottoTrasformato.getCommento());
            case "ELIMINATO_PRODOTTO_TRASFORMATO" ->
                    log.info("🗑️ Prodotto trasformato eliminato: {} (Creato da: {})",
                            prodottoTrasformato.getNome(), prodottoTrasformato.getCreatoDa());
            default ->
                    log.warn("⚠️ Evento sconosciuto '{}' per prodotto trasformato: {}", evento, prodottoTrasformato.getNome());
        }
    }
}
