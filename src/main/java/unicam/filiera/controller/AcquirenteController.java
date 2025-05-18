package unicam.filiera.controller;

import unicam.filiera.dao.*;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
import unicam.filiera.dto.DatiAcquistoDto;
import unicam.filiera.model.*;
import unicam.filiera.service.CarrelloService;
import unicam.filiera.service.CarrelloServiceImpl;
import unicam.filiera.service.PagamentoService;
import unicam.filiera.service.PagamentoServiceImpl;
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
    private final AcquistoDAO acquistoDAO = new JdbcAcquistoDAO();
    private final PagamentoService pagamentoService = new PagamentoServiceImpl();

    public AcquirenteController(PannelloAcquirente view, UtenteAutenticato utente) {
        this.view = view;
        this.utente = utente;
        this.carrelloService = new CarrelloServiceImpl();
        this.marketplaceCtrl = new MarketplaceController();
    }

    /**
     * Visualizza la lista marketplace (Prodotti + Pacchetti)
     */
    public void visualizzaMarketplace() {
        List<Object> lista = marketplaceCtrl.ottieniElementiMarketplace();
        view.showMarketplace(lista);
    }

    /**
     * Carica dal service il carrello e il totale, quindi li manda alla view
     */
    public void visualizzaCarrello() {
        List<CartItemDto> items = carrelloService.getCartItems();
        CartTotalsDto tot = carrelloService.calculateTotals();
        view.showCart(items, tot);
    }

    /**
     * Aggiunge un item e richiama loadCart(); in caso di eccezione invoca callback(false).
     */
    public void addToCart(Item item, int quantita, BiConsumer<String, Boolean> callback) {
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
        } catch (RuntimeException ex) {            // prende sia IAE che tutte le altre RTE
            callback.accept(ex.getMessage(), false);
        }
    }


    /**
     * Aggiorna la quantità di un item esistente; in caso di eccezione invoca callback(false).
     */
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
        } else {
            // 2b) annullato: non faccio nulla (oppure un messaggio facoltativo)
            callback.accept("Eliminazione annullata", true);
        }
    }

    /**
     * Aggiorna i fondi dell'acquirente (come prima), con callback di esito
     */
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


        // Prepara DTO
        DatiAcquistoDto dto = new DatiAcquistoDto(
                a.getUsername(),
                items,
                totale,
                metodo,
                StatoPagamento.IN_ATTESA,
                fondiPre,
                fondiPost
        );

        // Effettua il pagamento
        StatoPagamento esito = pagamentoService.effettuaPagamento(dto);
        dto.setStatoPagamento(esito);

        if (esito == StatoPagamento.APPROVATO) {
            // 1. Scala fondi e aggiorna db
            a.setFondi(fondiPost);
            UtenteDAO dao = JdbcUtenteDAO.getInstance();
            dao.aggiornaFondi(a.getUsername(), fondiPost);

            // 2. Aggiorna disponibilità degli item
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

            // 3. Salva acquisto e items
            acquistoDAO.salvaAcquisto(dto);

            // 4. Svuota il carrello
            carrelloService.clear();
            view.aggiornaFondi(fondiPost);
            callback.accept("Acquisto completato!", true);


        } else {
            callback.accept("Pagamento non riuscito!", false);
        }
    }
}
