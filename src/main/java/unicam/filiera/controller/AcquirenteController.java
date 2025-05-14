package unicam.filiera.controller;

import unicam.filiera.dao.JdbcUtenteDAO;
import unicam.filiera.dao.UtenteDAO;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
import unicam.filiera.model.Item;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.service.CarrelloService;
import unicam.filiera.service.CarrelloServiceImpl;
import unicam.filiera.util.ValidatoreAcquisto;
import unicam.filiera.util.ValidatoreMarketplace;
import unicam.filiera.view.PannelloAcquirente;

import javax.swing.*;
import java.util.List;
import java.util.function.BiConsumer;

public class AcquirenteController {
    private final CarrelloService carrelloService;
    private final PannelloAcquirente view;
    private final UtenteAutenticato utente;
    private final MarketplaceController marketplaceCtrl;

    public AcquirenteController(PannelloAcquirente view, UtenteAutenticato utente) {
        this.view            = view;
        this.utente          = utente;
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
        CartTotalsDto   tot   = carrelloService.calculateTotals();
        view.showCart(items, tot);
    }

    /**
     * Aggiunge un item e richiama loadCart(); in caso di eccezione invoca callback(false).
     */
    public void addToCart(Item item, int quantita, BiConsumer<String,Boolean> callback) {
        try {
            // 1) validazione tipo + nome
            String tipo = (item instanceof Prodotto) ? "Prodotto" : "Pacchetto";
            ValidatoreMarketplace.validaTipo(tipo);
            ValidatoreMarketplace.validaNome(item.getNome());

            // 2) validazione quantità
            ValidatoreAcquisto.validaQuantitaItem(item, quantita);

            // 3) effettiva aggiunta
            carrelloService.addItem(item, quantita);

            // 4) ricarico il carrello in view
            List<CartItemDto> items = carrelloService.getCartItems();
            CartTotalsDto tot = carrelloService.calculateTotals();
            view.showCart(items, tot);

            callback.accept(item.getNome() + " aggiunto al carrello", true);
        }
        catch (RuntimeException ex) {            // prende sia IAE che tutte le altre RTE
            callback.accept(ex.getMessage(), false);
        }
    }


    /**
     * Aggiorna la quantità di un item esistente; in caso di eccezione invoca callback(false).
     */
    public void updateCartItem(String nomeItem, int nuovaQta, BiConsumer<String,Boolean> callback) {
        try {
            carrelloService.updateItemQuantity(nomeItem, nuovaQta);
            visualizzaCarrello();
            callback.accept("Quantità di \"" + nomeItem + "\" aggiornata", true);
        } catch (IllegalArgumentException ex) {
            callback.accept(ex.getMessage(), false);
        }
    }

    public void requestDeleteCartItem(String nomeItem, BiConsumer<String,Boolean> callback) {
        // 1) chiedo la conferma all'utente **qui**, nel controller
        int choice = JOptionPane.showConfirmDialog(
                /* parent = */ view,
                "Sei sicuro di voler eliminare \"" + nomeItem + "\" dal carrello?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            // 2a) confermato: chiamo il solito delete + ricarico
            carrelloService.removeItem(nomeItem);
            visualizzaCarrello();
            callback.accept(nomeItem + " rimosso dal carrello", true);
        }
        else {
            // 2b) annullato: non faccio nulla (oppure un messaggio facoltativo)
            callback.accept("Eliminazione annullata", true);
        }
    }

    /** Aggiorna i fondi dell'acquirente (come prima), con callback di esito */
    public void aggiornaFondiAcquirente(double nuoviFondi, BiConsumer<String,Boolean> callback) {
        if (!(utente instanceof unicam.filiera.model.Acquirente a)) {
            callback.accept("L'utente non è un acquirente", false);
            return;
        }
        try {
            a.setFondi(nuoviFondi);
            UtenteDAO dao = JdbcUtenteDAO.getInstance();
            dao.aggiornaFondi(a.getUsername(), nuoviFondi);
            callback.accept("Fondi aggiornati: €" + nuoviFondi, true);
        } catch (Exception e) {
            callback.accept("Errore nell'aggiornamento fondi", false);
        }
    }
}
