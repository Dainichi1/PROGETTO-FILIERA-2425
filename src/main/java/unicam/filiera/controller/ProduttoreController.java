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
     * Corrisponde a createProdotto() nel diagramma UML.
     * Istanzia un nuovo oggetto prodotto.
     */
    public Prodotto creaNuovoProdotto(String nome,
                                   String descrizione,
                                   int quantita,
                                   double prezzo,
                                   String creatoDa) {
        if (nome == null || nome.isEmpty()) throw new IllegalArgumentException("Nome mancante");
        if (descrizione == null || descrizione.isEmpty()) throw new IllegalArgumentException("Descrizione mancante");
        if (quantita < 1) throw new IllegalArgumentException("Quantità non valida");
        if (prezzo < 0) throw new IllegalArgumentException("Prezzo non valido");

        return new Prodotto(nome, descrizione, quantita, prezzo, null, null, creatoDa, StatoProdotto.IN_ATTESA);
    }

    /**
     * Corrisponde a inviaDatiProdotto() nel diagramma UML.
     */
    public boolean inviaDatiProdotto(Prodotto prodotto) {
        return prodottoDAO.salvaDettagli(prodotto);
    }

    /**
     * Corrisponde a uploadFile(files) nel diagramma UML.
     */
    public boolean uploadFile(List<File> certificati, List<File> foto, Prodotto prodotto) {
        return prodottoDAO.salvaFile(certificati, foto, prodotto);
    }

    /**
     * Corrisponde a inviaModuloAlCuratore() → inoltraModulo()
     */
    public boolean inoltraModulo(Prodotto prodotto) {
        return prodottoDAO.aggiornaStatoProdotto(prodotto, StatoProdotto.IN_ATTESA);
    }

    /**
     * Corrisponde a inviaNuovoProdotto() nel diagramma UML.
     */
    public boolean inviaNuovoProdotto(Prodotto prodotto) {
        // Potresti aggiungere logiche aggiuntive prima di inserirlo in lista approvati
        return prodottoDAO.aggiungiInListaApprovazioni(prodotto);
    }

    /**
     * Recupera i prodotti creati da un produttore.
     */
    public List<Prodotto> getProdottiCreatiDa(String username) {
        return prodottoDAO.getProdottiByCreatore(username);
    }
}
