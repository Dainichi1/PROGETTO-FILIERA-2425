package unicam.filiera.controller;

import unicam.filiera.dao.*;
import unicam.filiera.dto.*;
import unicam.filiera.model.*;
import unicam.filiera.service.CarrelloService;
import unicam.filiera.service.CarrelloServiceImpl;
import unicam.filiera.service.PagamentoService;
import unicam.filiera.service.PagamentoServiceImpl;
import unicam.filiera.util.ValidatoreAcquisto;
import unicam.filiera.util.ValidatoreMarketplace;
import unicam.filiera.util.ValidatorePrenotazioneFiera;
import unicam.filiera.util.ValidatoreRecensione;
import unicam.filiera.view.PannelloAcquirente;

import javax.swing.*;
import java.util.List;
import java.util.function.BiConsumer;

public class AcquirenteController {
    private final CarrelloService carrelloService;
    private final PannelloAcquirente view;
    private final UtenteAutenticato utente;
    private final MarketplaceController marketplaceCtrl;
    private final AcquistoDAO acquistoDAO = new JdbcAcquistoDAO();
    private final PagamentoService pagamentoService = new PagamentoServiceImpl();

    public AcquirenteController(PannelloAcquirente view, UtenteAutenticato utente) {
        this.view = view;
        this.utente = utente;
        this.carrelloService = new CarrelloServiceImpl();
        this.marketplaceCtrl = new MarketplaceController();
    }

    /** Visualizza la lista marketplace (Prodotti + Pacchetti) */
    public void visualizzaMarketplace() {
        List<Object> lista = marketplaceCtrl.ottieniElementiMarketplace();
        view.showMarketplace(lista);
    }

    /** Carica dal service il carrello e il totale, quindi li manda alla view */
    public void visualizzaCarrello() {
        List<CartItemDto> items = carrelloService.getCartItems();
        CartTotalsDto tot = carrelloService.calculateTotals();
        view.showCart(items, tot);
    }

    /** Aggiunge un item e ricarica il carrello; in caso di eccezione invoca callback(false). */
    public void addToCart(Item item, int quantita, BiConsumer<String, Boolean> callback) {
        try {
            String tipo = (item instanceof Prodotto) ? "Prodotto" : "Pacchetto";
            ValidatoreMarketplace.validaTipo(tipo);
            ValidatoreMarketplace.validaNome(item.getNome());
            ValidatoreAcquisto.validaQuantitaItem(item, quantita);

            carrelloService.addItem(item, quantita);

            List<CartItemDto> items = carrelloService.getCartItems();
            CartTotalsDto tot = carrelloService.calculateTotals();
            view.showCart(items, tot);

            callback.accept(item.getNome() + " aggiunto al carrello", true);
        } catch (RuntimeException ex) {
            callback.accept(ex.getMessage(), false);
        }
    }

    /** Aggiorna la quantità di un item esistente; in caso di eccezione invoca callback(false). */
    public void updateCartItem(String nomeItem, int nuovaQta, BiConsumer<String, Boolean> callback) {
        try {
            carrelloService.updateItemQuantity(nomeItem, nuovaQta);
            visualizzaCarrello();
            callback.accept("Quantità di \"" + nomeItem + "\" aggiornata", true);
        } catch (IllegalArgumentException ex) {
            callback.accept(ex.getMessage(), false);
        }
    }

    public void requestDeleteCartItem(String nomeItem, BiConsumer<String, Boolean> callback) {
        int choice = JOptionPane.showConfirmDialog(
                view,
                "Sei sicuro di voler eliminare \"" + nomeItem + "\" dal carrello?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            carrelloService.removeItem(nomeItem);
            visualizzaCarrello();
            callback.accept(nomeItem + " rimosso dal carrello", true);
        } else {
            callback.accept("Eliminazione annullata", true);
        }
    }

    /** Aggiorna i fondi dell'acquirente, con callback di esito. */
    public void aggiornaFondiAcquirente(double nuoviFondi, BiConsumer<String, Boolean> callback) {
        if (!(utente instanceof unicam.filiera.model.Acquirente a)) {
            callback.accept("L'utente non è un acquirente", false);
            return;
        }
        try {
            a.setFondi(nuoviFondi);
            UtenteDAO dao = JdbcUtenteDAO.getInstance();
            dao.aggiornaFondi(a.getUsername(), nuoviFondi);
            view.aggiornaFondi(nuoviFondi);
            callback.accept("Fondi aggiornati: €" + nuoviFondi, true);
        } catch (Exception e) {
            callback.accept("Errore nell'aggiornamento fondi", false);
        }
    }

    // Chiamato dalla view quando l’utente conferma il pagamento
    public void effettuaAcquisto(TipoMetodoPagamento metodo, BiConsumer<String, Boolean> callback) {
        if (!(utente instanceof Acquirente a)) {
            callback.accept("Utente non valido", false);
            return;
        }
        List<CartItemDto> items = carrelloService.getCartItems();
        double totale = carrelloService.calculateTotals().getCostoTotale();
        double fondiPre = a.getFondi();
        double fondiPost = fondiPre - totale;

        try {
            ValidatoreAcquisto.validaFondi(fondiPre, totale);
        } catch (IllegalArgumentException ex) {
            callback.accept(ex.getMessage(), false);
            return;
        }

        DatiAcquistoDto dto = new DatiAcquistoDto(
                a.getUsername(), items, totale, metodo,
                StatoPagamento.IN_ATTESA, fondiPre, fondiPost
        );

        StatoPagamento esito = pagamentoService.effettuaPagamento(dto);
        dto.setStatoPagamento(esito);

        if (esito == StatoPagamento.APPROVATO) {
            a.setFondi(fondiPost);
            UtenteDAO dao = JdbcUtenteDAO.getInstance();
            dao.aggiornaFondi(a.getUsername(), fondiPost);

            for (CartItemDto c : items) {
                if ("Prodotto".equals(c.getTipo())) {
                    ProdottoDAO prodottoDAO = JdbcProdottoDAO.getInstance();
                    Prodotto prodotto = prodottoDAO.findByNome(c.getNome());
                    int nuovaQta = prodotto.getQuantita() - c.getQuantita();
                    prodottoDAO.aggiornaQuantita(prodotto.getNome(), nuovaQta);
                    ObserverManagerItem.notificaAggiornamento(prodotto.getNome(), "QUANTITA_AGGIORNATA");
                } else if ("Pacchetto".equals(c.getTipo())) {
                    PacchettoDAO pacchettoDAO = JdbcPacchettoDAO.getInstance();
                    Pacchetto pacchetto = pacchettoDAO.findByNomeAndCreatore(c.getNome(), "");
                    int nuovaQta = pacchetto.getQuantita() - c.getQuantita();
                    pacchettoDAO.aggiornaQuantita(pacchetto.getNome(), nuovaQta);
                    ObserverManagerItem.notificaAggiornamento(pacchetto.getNome(), "QUANTITA_AGGIORNATA");
                }
            }

            acquistoDAO.salvaAcquisto(dto);

            carrelloService.clear();
            view.aggiornaFondi(fondiPost);
            callback.accept("Acquisto completato!", true);
        } else {
            callback.accept("Pagamento non riuscito!", false);
        }
    }

    public void visualizzaFiereDisponibili() {
        List<Fiera> fiere = marketplaceCtrl.ottieniFiereDisponibili();
        view.showFiereDisponibili(fiere);
    }

    public void prenotaIngressoFiera(long idFiera, int numeroPersone, BiConsumer<String, Boolean> callback) {
        if (!(utente instanceof Acquirente a)) {
            callback.accept("Utente non valido", false);
            return;
        }

        FieraDAO fieraDAO = JdbcFieraDAO.getInstance();
        Fiera fiera = fieraDAO.findById(idFiera);
        PrenotazioneFieraDAO prenDAO = new JdbcPrenotazioneFieraDAO();

        try {
            ValidatorePrenotazioneFiera.validaPrenotazione(
                    idFiera, numeroPersone, fiera, a.getUsername(), prenDAO, a.getFondi()
            );

            double costoTotale = fiera.getPrezzo() * numeroPersone;

            PrenotazioneFiera pren = new PrenotazioneFiera(
                    idFiera, a.getUsername(), numeroPersone, java.time.LocalDateTime.now()
            );
            boolean ok = prenDAO.save(pren);

            if (ok) {
                double nuoviFondi = a.getFondi() - costoTotale;
                a.setFondi(nuoviFondi);
                UtenteDAO dao = JdbcUtenteDAO.getInstance();
                dao.aggiornaFondi(a.getUsername(), nuoviFondi);
                view.aggiornaFondi(nuoviFondi);
                callback.accept("Prenotazione effettuata con successo!", true);
            } else {
                callback.accept("Errore durante la prenotazione.", false);
            }
        } catch (IllegalArgumentException ex) {
            callback.accept(ex.getMessage(), false);
        }
    }

    public void eliminaPrenotazioneFiera(long idPrenotazione, BiConsumer<String, Boolean> callback) {
        if (!(utente instanceof Acquirente a)) {
            callback.accept("Utente non valido", false);
            return;
        }

        PrenotazioneFieraDAO prenDAO = new JdbcPrenotazioneFieraDAO();
        PrenotazioneFiera pren = prenDAO.findById(idPrenotazione);
        if (pren == null) {
            callback.accept("Prenotazione non trovata.", false);
            return;
        }

        FieraDAO fieraDAO = JdbcFieraDAO.getInstance();
        Fiera fiera = fieraDAO.findById(pren.getIdFiera());
        if (fiera == null) {
            callback.accept("Fiera associata non trovata.", false);
            return;
        }

        boolean deleted = prenDAO.delete(idPrenotazione);
        if (deleted) {
            double rimborso = fiera.getPrezzo() * pren.getNumeroPersone();
            double nuoviFondi = a.getFondi() + rimborso;
            a.setFondi(nuoviFondi);

            UtenteDAO utenteDAO = JdbcUtenteDAO.getInstance();
            utenteDAO.aggiornaFondi(a.getUsername(), nuoviFondi);
            view.aggiornaFondi(nuoviFondi);

            callback.accept("Prenotazione eliminata e fondi rimborsati: €" + String.format("%.2f", rimborso), true);
        } else {
            callback.accept("Errore durante l'eliminazione della prenotazione.", false);
        }
    }

    public void visualizzaPrenotazioniFiere() {
        if (!(utente instanceof Acquirente a)) return;
        PrenotazioneFieraDAO prenDAO = new JdbcPrenotazioneFieraDAO();
        List<PrenotazioneFiera> lista = prenDAO.findByUsername(a.getUsername());
        List<Fiera> tutteLeFiere = JdbcFieraDAO.getInstance().findAll();
        view.showPrenotazioniFiere(lista, tutteLeFiere);
    }

    public void visualizzaAcquisti() {
        if (!(utente instanceof Acquirente a)) {
            JOptionPane.showMessageDialog(view, "Utente non valido", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<AcquistoListaDto> lista = acquistoDAO.findByUsername(a.getUsername());
        view.showAcquistiDialog(lista);  // la view apre la dialog master/dettaglio
    }

    /** Chiamato dalla view quando seleziona un acquisto nella tabella master */
    public void caricaDettaglioAcquisto(int idAcquisto) {
        List<AcquistoItemDto> items = acquistoDAO.findItemsByAcquisto(idAcquisto);
        view.updateDettaglioAcquisto(items);
    }

    public void visualizzaSocialNetwork() {
        try (var conn = DatabaseManager.getConnection()) {
            var dao = new unicam.filiera.dao.JdbcSocialPostDAO(conn);
            var posts = dao.findAllOrderByDataDesc();
            view.showSocialNetworkDialog(posts);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view,
                    "Errore nel caricamento del social network",
                    "Errore", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /** Step 7–11: conferma, valida difensiva e pubblicazione recensione */
    public void lasciaRecensione(PostSocialDto post) {
        try {
            // fallback autore dal contesto se non valorizzato dal form
            if (post.getAutoreUsername() == null || post.getAutoreUsername().isBlank()) {
                post.setAutoreUsername(getUsername());
            }

            // Validazione difensiva lato controller
            ValidatoreRecensione.valida(post);

            // Step 8: conferma pubblicazione
            int choice = JOptionPane.showConfirmDialog(
                    view,
                    "Sei sicuro di voler pubblicare la recensione sul Social?",
                    "Conferma pubblicazione",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) {
                // 8.a: annullato
                return;
            }

            // Step 10: persistenza
            try (var conn = DatabaseManager.getConnection()) {
                var dao = new unicam.filiera.dao.JdbcSocialPostDAO(conn);
                dao.pubblicaPost(post);
            }

            // Step 11: feedback all'utente
            JOptionPane.showMessageDialog(view,
                    "Recensione pubblicata con successo!",
                    "Successo", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(),
                    "Errore di validazione", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException | java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(view,
                    "Errore durante il salvataggio della recensione",
                    "Errore", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public String getUsername() {
        return (utente != null) ? utente.getUsername() : null;
    }
}
