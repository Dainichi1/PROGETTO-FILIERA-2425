// unicam/filiera/service/CarrelloService.java
package unicam.filiera.service;

import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
import unicam.filiera.model.Item;

import java.util.List;

public interface CarrelloService {
    /**
     * Aggiunge l'item con quantità, o rilancia IllegalArgumentException
     */
    void addItem(Item item, int quantita);

    /**
     * Aggiorna la quantità di un item esistente, o rilancia IllegalArgumentException
     */
    void updateItemQuantity(String nomeItem, int nuovaQuantita);

    /**
     * Rimuove l'item dal carrello
     */
    void removeItem(String nomeItem);

    /**
     * Restituisce la lista di tutti gli item correnti
     */
    List<CartItemDto> getCartItems();

    /**
     * Calcola e restituisce i totali
     */
    CartTotalsDto calculateTotals();
}
