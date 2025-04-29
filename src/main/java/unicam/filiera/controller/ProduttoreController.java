/* ==================================================================== */
/*  ProduttoreController.java                                           */
/* ==================================================================== */
package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;
import unicam.filiera.util.ValidatoreProdotto;

import java.io.File;
import java.util.List;

public class ProduttoreController {

    /* ------------------------- callback UI -------------------------- */
    @FunctionalInterface
    public interface EsitoListener {
        /** @param ok  TRUE = operazione ok – FALSE = errore              */
        void completato(Boolean ok, String msg);   // ← wrapper Boolean
    }

    /* ---------------------------------------------------------------- */
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();
    private final String      usernameProduttore;

    public ProduttoreController(String usernameProduttore) {
        this.usernameProduttore = usernameProduttore;
    }

    /* ======================  USE-CASE PRINCIPALE  =================== */
    public void inviaProdotto(String  nome, String descrizione,
                              String  quantitaTxt, String prezzoTxt,
                              String  indirizzo,
                              List<File> certificati, List<File> foto,
                              EsitoListener callback) {

        try {
            /* --- parsing & validazione --- */
            int    quantita = Integer.parseInt(quantitaTxt);
            double prezzo   = Double.parseDouble(prezzoTxt);

            ValidatoreProdotto.valida(nome, descrizione, indirizzo, quantita, prezzo);
            ValidatoreProdotto.validaFileCaricati(certificati.size(), foto.size());

            /* --- costruzione dominio (Builder) --- */
            Prodotto prodotto = creaNuovoProdotto(
                    nome, descrizione, quantita, prezzo, indirizzo, usernameProduttore);

            /* --- persistenza & notifica --- */
            boolean ok =
                    inviaDatiProdotto(prodotto) &&
                            uploadFile(certificati, foto, prodotto) &&
                            inoltraModulo(prodotto)      &&
                            inviaNuovoProdotto(prodotto);

            callback.completato(ok,
                    ok ? "Prodotto inviato al Curatore!"
                            : "Errore durante il salvataggio.");

        } catch (NumberFormatException nfe) {
            callback.completato(false, "Quantità o prezzo non numerici.");
        } catch (IllegalArgumentException iae) {
            callback.completato(false, iae.getMessage());
        } catch (Exception ex) {
            callback.completato(false, "Errore: " + ex.getMessage());
        }
    }

    /* =================  METODI GRANULARI (immutati)  ================ */
    public Prodotto creaNuovoProdotto(String nome, String descrizione,
                                      int quantita, double prezzo,
                                      String indirizzo, String creatoDa) {
        ValidatoreProdotto.valida(nome, descrizione, indirizzo, quantita, prezzo);
        return new Prodotto.Builder()
                .nome(nome).descrizione(descrizione)
                .quantita(quantita).prezzo(prezzo)
                .indirizzo(indirizzo)
                .creatoDa(creatoDa)
                .stato(StatoProdotto.IN_ATTESA)
                .build();
    }

    public boolean inviaDatiProdotto(Prodotto p) {
        return prodottoDAO.salvaDettagli(p);
    }

    public boolean uploadFile(List<File> cert, List<File> foto, Prodotto p) {
        ValidatoreProdotto.validaFileCaricati(cert.size(), foto.size());
        return prodottoDAO.salvaFile(cert, foto, p);
    }

    public boolean inoltraModulo(Prodotto p) {
        boolean ok = prodottoDAO.aggiornaStatoProdotto(p, StatoProdotto.IN_ATTESA);
        if (ok) ProdottoNotifier.getInstance().notificaTutti(p, "NUOVO_PRODOTTO");
        return ok;
    }

    public boolean inviaNuovoProdotto(Prodotto p) {
        return prodottoDAO.aggiungiInListaApprovazioni(p);
    }

    public List<Prodotto> getProdottiCreatiDaMe() {
        return prodottoDAO.getProdottiByCreatore(usernameProduttore);
    }
}
