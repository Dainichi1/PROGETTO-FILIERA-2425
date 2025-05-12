package unicam.filiera.controller;


import unicam.filiera.dao.JdbcUtenteDAO;
import unicam.filiera.dao.UtenteDAO;
import unicam.filiera.model.*;

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

    public void aggiungiAlCarrello(Item item, int quantita) {
        double prezzoUnitario = 0.0;
        String tipo;

        // Determina il tipo e il prezzo in base all'istanza
        if (item instanceof Prodotto p) {
            prezzoUnitario = p.getPrezzo();
            tipo = "Prodotto";
        } else if (item instanceof Pacchetto pk) {
            prezzoUnitario = pk.getPrezzoTotale();
            tipo = "Pacchetto";
        } else {
            throw new IllegalArgumentException("Tipo di item non supportato");
        }

        double prezzoTotale = prezzoUnitario * quantita;

        carrello.add(new Object[]{
                tipo,
                item.getNome(),
                quantita,
                prezzoTotale
        });

        view.showCarrello(carrello);
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





}
