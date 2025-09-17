package unicam.filiera.service;

import jakarta.servlet.http.HttpSession;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
import unicam.filiera.dto.ItemTipo;

import java.util.List;

public interface CarrelloService {

    /**
     * Aggiunge un item al carrello.
     */
    void aggiungiItem(ItemTipo tipo, Long id, int quantita, HttpSession session);

    /**
     * Aggiorna la quantità di un item esistente.
     */
    void aggiornaQuantitaItem(String nomeItem, int nuovaQuantita, HttpSession session);

    /**
     * Rimuove un item dal carrello.
     */
    void rimuoviItem(String nomeItem, HttpSession session);

    /**
     * Svuota il carrello.
     */
    void svuota(HttpSession session);

    /**
     * Restituisce gli item correnti.
     */
    List<CartItemDto> getItems(HttpSession session);

    /**
     * Calcola i totali.
     */
    CartTotalsDto calcolaTotali(HttpSession session);
}
