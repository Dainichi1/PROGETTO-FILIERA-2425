package unicam.filiera.service;

import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
import unicam.filiera.model.Item;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.util.ValidatoreAcquisto;
import unicam.filiera.controller.MarketplaceController;  // “repo” degli item di mercato

import java.util.ArrayList;
import java.util.List;

public class CarrelloServiceImpl implements CarrelloService {

    private final MarketplaceController marketplace;
    private final List<CartItemDto> items;

    /**
     * Costruttore di iniezione: riceve il controller del marketplace
     */
    public CarrelloServiceImpl(MarketplaceController marketplace) {
        this.marketplace = marketplace;
        this.items = new ArrayList<>();
    }

    /**
     * Costruttore di convenienza: usa l’istanza di default di MarketplaceController
     */
    public CarrelloServiceImpl() {
        this(new MarketplaceController());
    }

    @Override
    public void addItem(Item item, int quantita) {
        ValidatoreAcquisto.validaQuantitaItem(item, quantita);
        double prezzoUnit = (item instanceof Prodotto p)
                ? p.getPrezzo()
                : ((Pacchetto) item).getPrezzoTotale();
        items.add(new CartItemDto(
                item instanceof Prodotto ? "Prodotto" : "Pacchetto",
                item.getNome(),
                quantita,
                prezzoUnit
        ));
    }

    @Override
    public void updateItemQuantity(String nomeItem, int nuovaQuantita) {
        // 1) recupera il DTO esistente
        CartItemDto old = items.stream()
                .filter(i -> i.getNome().equals(nomeItem))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item non trovato"));

        // 2) recupera l’Item “vero” dal marketplace
        Item reale = marketplace.ottieniElementiMarketplace().stream()
                .filter(o -> o instanceof Item it && it.getNome().equals(nomeItem))
                .map(o -> (Item) o)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Impossibile trovare il prodotto"));

        // 3) validazione sul vero oggetto
        ValidatoreAcquisto.validaQuantitaItem(reale, nuovaQuantita);

        // 4) sostituisci il DTO con la nuova quantità
        items.remove(old);
        items.add(new CartItemDto(
                old.getTipo(),
                nomeItem,
                nuovaQuantita,
                old.getPrezzoUnitario()
        ));
    }

    @Override
    public void removeItem(String nomeItem) {
        items.removeIf(i -> i.getNome().equals(nomeItem));
    }

    @Override
    public List<CartItemDto> getCartItems() {
        return List.copyOf(items);
    }

    @Override
    public CartTotalsDto calculateTotals() {
        int totQta = items.stream().mapToInt(CartItemDto::getQuantita).sum();
        double totCost = items.stream().mapToDouble(CartItemDto::getTotale).sum();
        return new CartTotalsDto(totQta, totCost);
    }

    @Override
    public void clear() {
        items.clear();
    }

}
