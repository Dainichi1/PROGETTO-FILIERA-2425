package unicam.filiera.controller;


import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.UtenteAutenticato;

import unicam.filiera.view.PannelloAcquirente;

import java.util.List;
import java.util.ArrayList;


public class AcquirenteController {
    private final MarketplaceController marketplaceCtrl;


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

    private final List<Object[]> carrello = new ArrayList<>();

    public void aggiungiAlCarrello(String nome, String tipo, int quantita) {
        double prezzoUnitario = 0.0;

        // Cerca il prodotto o pacchetto nel marketplace per ottenere il prezzo unitario
        for (Object o : marketplaceCtrl.ottieniElementiMarketplace()) {
            if (tipo.equals("Prodotto") && o instanceof Prodotto p && p.getNome().equals(nome)) {
                prezzoUnitario = p.getPrezzo();
                break;
            } else if (tipo.equals("Pacchetto") && o instanceof Pacchetto pk && pk.getNome().equals(nome)) {
                prezzoUnitario = pk.getPrezzoTotale();
                break;
            }
        }

        // Calcola il prezzo totale (prezzo unitario × quantità)
        double prezzoTotale = prezzoUnitario * quantita;

        // Aggiungi al carrello (salva il prezzo totale)
        carrello.add(new Object[]{tipo, nome, quantita, prezzoTotale});

        // Chiedi alla view di aggiornare il carrello
        view.showCarrello(carrello);
    }


}
