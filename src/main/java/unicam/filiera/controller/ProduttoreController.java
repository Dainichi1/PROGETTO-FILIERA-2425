package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;
import unicam.filiera.util.ValidatoreProdotto;

import java.io.File;
import java.util.List;

public class ProduttoreController {

    private final ProdottoDAO prodottoDAO;

    public ProduttoreController() {
        this.prodottoDAO = new ProdottoDAO();
    }

    /**
     * Corrisponde a createProdotto() nel diagramma UML.
     */
    public Prodotto creaNuovoProdotto(String nome,
                                      String descrizione,
                                      int quantita,
                                      double prezzo,
                                      String indirizzo,
                                      String creatoDa) {
        // Validazione centralizzata
        ValidatoreProdotto.valida(nome, descrizione, indirizzo, quantita, prezzo);

        return new Prodotto.Builder()
                .nome(nome)
                .descrizione(descrizione)
                .quantita(quantita)
                .prezzo(prezzo)
                .indirizzo(indirizzo)
                .creatoDa(creatoDa)
                .stato(StatoProdotto.IN_ATTESA)
                .build();
    }

    /**
     * Salva i dettagli base del prodotto (senza file).
     */
    public boolean inviaDatiProdotto(Prodotto prodotto) {
        return prodottoDAO.salvaDettagli(prodotto);
    }

    /**
     * Salva i file nel filesystem e aggiorna il DB.
     */
    public boolean uploadFile(List<File> certificati, List<File> foto, Prodotto prodotto) {
        // Validazione dei file
        ValidatoreProdotto.validaFileCaricati(certificati.size(), foto.size());

        return prodottoDAO.salvaFile(certificati, foto, prodotto);
    }

    /**
     * Notifica che il prodotto è in attesa del curatore.
     */
    public boolean inoltraModulo(Prodotto prodotto) {
        boolean success = prodottoDAO.aggiornaStatoProdotto(prodotto, StatoProdotto.IN_ATTESA);

        if (success) {
            ProdottoNotifier.getInstance().notificaTutti(prodotto, "NUOVO_PRODOTTO");
        }

        return success;
    }

    /**
     * Per coerenza, questa operazione potrebbe anche essere accorpata a inoltraModulo()
     */
    public boolean inviaNuovoProdotto(Prodotto prodotto) {
        return prodottoDAO.aggiungiInListaApprovazioni(prodotto);
    }

    /**
     * Recupera i prodotti creati da un produttore specifico.
     */
    public List<Prodotto> getProdottiCreatiDa(String username) {
        return prodottoDAO.getProdottiByCreatore(username);
    }
}
