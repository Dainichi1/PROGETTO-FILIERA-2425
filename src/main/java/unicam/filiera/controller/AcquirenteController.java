package unicam.filiera.controller;


import unicam.filiera.dao.JdbcUtenteDAO;
import unicam.filiera.dao.UtenteDAO;
import unicam.filiera.model.*;
import unicam.filiera.model.TotaleCarrello;
import unicam.filiera.util.ValidatoreAcquisto;
import unicam.filiera.view.PannelloAcquirente;

import java.util.List;
import java.util.ArrayList;


public class AcquirenteController {
    private final MarketplaceController marketplaceCtrl;
    private final List<Item> itemNelCarrello = new ArrayList<>();
    private final List<Object[]> carrello = new ArrayList<>();


    private final PannelloAcquirente view;
    private final UtenteAutenticato utente;

    public AcquirenteController(PannelloAcquirente view, UtenteAutenticato utente) {
        this.view = view;
        this.utente = utente;
        this.marketplaceCtrl = new MarketplaceController();


    }

    public void visualizzaMarketplace() {
        List<Object> lista = marketplaceCtrl.ottieniElementiMarketplace(); // prodotti + pacchetti
        view.showMarketplace(lista);
    }


    public void aggiungiAlCarrello(Item item, int quantita) {
        ValidatoreAcquisto.validaQuantitaItem(item, quantita);

        double prezzoUnitario = item instanceof Prodotto p ? p.getPrezzo() :
                item instanceof Pacchetto pk ? pk.getPrezzoTotale() : 0.0;
        String tipo = item instanceof Prodotto ? "Prodotto" : "Pacchetto";

        carrello.add(new Object[]{tipo,
                item.getNome(),
                quantita,
                prezzoUnitario * quantita,
                "Conferma"});
        itemNelCarrello.add(item);

        // 1) ridisegno la tabella del carrello…
        view.showCarrello(carrello);

        // 2) …e subito dopo ricalcolo e mostro i totali
        TotaleCarrello tot = calculateTotals();
        view.showTotali(tot);
    }


    public Item getItemNelCarrelloByNome(String nome) {
        return itemNelCarrello.stream()
                .filter(i -> i.getNome().equals(nome))
                .findFirst()
                .orElse(null);
    }


    public UtenteAutenticato getUtente() {
        return utente;
    }

    public void aggiornaFondiAcquirente(double nuoviFondi) {
        if (utente instanceof Acquirente a) {
            a.setFondi(nuoviFondi); // ora il metodo esiste
            UtenteDAO dao = JdbcUtenteDAO.getInstance();
            dao.aggiornaFondi(a.getUsername(), nuoviFondi);
        } else {
            throw new IllegalStateException("L'utente non è un acquirente");
        }
    }

    /**
     * Aggiorna la quantità di un item nel carrello.
     */
    public void aggiornaQuantitaItemNelCarrello(Item item, int nuovaQuantita) {
        for (Object[] riga : carrello) {
            if (riga[1].equals(item.getNome())) {
                double prezzoUnitario = ((double) riga[3]) / ((int) riga[2]); // prezzo / quantità corrente
                riga[2] = nuovaQuantita;
                riga[3] = prezzoUnitario * nuovaQuantita;
                riga[4] = "Conferma";
                view.showCarrello(carrello);
                TotaleCarrello tot = calculateTotals();
                view.showTotali(tot);
                return;
            }
        }
    }


    public void rimuoviItemDalCarrello(Item item) {
        carrello.removeIf(riga -> riga[1].equals(item.getNome()));
        itemNelCarrello.removeIf(i -> i.getNome().equals(item.getNome()));
        view.showCarrello(carrello);
        TotaleCarrello tot = calculateTotals();
        view.showTotali(tot);

    }


    public Item getItemByNome(String tipo, String nome) {
        return marketplaceCtrl.ottieniElementiMarketplace().stream()
                .filter(i -> i instanceof Item item && item.getNome().equals(nome) &&
                        ((tipo.equals("Prodotto") && item instanceof Prodotto) ||
                                (tipo.equals("Pacchetto") && item instanceof Pacchetto)))
                .map(i -> (Item) i)
                .findFirst()
                .orElse(null);
    }

    /**
     * Mostra in view lo stato corrente del carrello interno.
     */
    public void visualizzaCarrello() {
        view.showCarrello(carrello);
    }

    public TotaleCarrello calculateTotals() {
        int totaleQuantita = carrello.stream()
                .mapToInt(riga -> (int) riga[2])
                .sum();
        double totaleCosto = carrello.stream()
                .mapToDouble(riga -> (double) riga[3])
                .sum();
        return new TotaleCarrello(totaleQuantita, totaleCosto);
    }

    /** Chiamato dalla view quando l’utente clicca “Elimina” */
    public void requestDeleteItem(Item item) {
        view.askDeleteConfirmation(item);
    }

    /** Se l’utente conferma, rimuove e aggiorna view + totali */
    public void deleteItem(Item item) {
        rimuoviItemDalCarrello(item);
        // rimuoviItemDalCarrello già fa showCarrello() e showTotali()
        view.avvisaSuccesso(item.getNome() + " eliminato dal carrello.");
    }

    /** Se l’utente annulla, torno alla home (marketplace) */
    public void cancelDelete() {
        view.showMarketplace(marketplaceCtrl.ottieniElementiMarketplace());
    }


}
