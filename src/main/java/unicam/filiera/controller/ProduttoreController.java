package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.io.File;
import java.util.List;

public class ProduttoreController {

    private final ProdottoDAO prodottoDAO;

    public ProduttoreController() {
        this.prodottoDAO = new ProdottoDAO();
    }

    /**
     * Crea un nuovo prodotto e lo salva nel DB, restituendo true/false a seconda del successo.
     */
    public boolean creaNuovoProdotto(String nome,
                                     String descrizione,
                                     int quantita,
                                     double prezzo,
                                     List<File> certificati,
                                     List<File> foto,
                                     String creatoDa)
    {
        // Eventuali controlli “business”
        if (nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("Nome del prodotto mancante!");
        }
        if (descrizione == null || descrizione.isEmpty()) {
            throw new IllegalArgumentException("Descrizione mancante!");
        }
        if (quantita < 1) {
            throw new IllegalArgumentException("Quantità non valida!");
        }
        if (prezzo < 0) {
            throw new IllegalArgumentException("Prezzo non valido!");
        }

        // Creiamo l'oggetto Prodotto
        Prodotto prodotto = new Prodotto(
                nome,
                descrizione,
                quantita,
                prezzo,
                null,    // i certificati/foto sono elenchi di stringhe, li gestirà il DAO quando copia i file
                null,
                creatoDa,
                StatoProdotto.IN_ATTESA
        );

        // Salviamo nel DB
        return prodottoDAO.salvaProdotto(prodotto, certificati, foto);
    }

    /**
     * Restituisce tutti i prodotti creati dal produttore con username indicato.
     */
    public List<Prodotto> getProdottiCreatiDa(String username) {
        return prodottoDAO.getProdottiByCreatore(username);
    }
}
