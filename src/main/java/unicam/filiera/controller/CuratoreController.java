package unicam.filiera.controller;

import unicam.filiera.dao.PacchettoDAO;
import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.PacchettoNotifier;
import unicam.filiera.model.observer.ProdottoNotifier;

import java.util.List;

public class CuratoreController {

    private final ProdottoDAO prodottoDAO;
    private final PacchettoDAO pacchettoDAO;

    public CuratoreController() {
        this.prodottoDAO = new ProdottoDAO();
        this.pacchettoDAO = new PacchettoDAO();
    }

    public List<Prodotto> getProdottiDaApprovare() {
        return prodottoDAO.getProdottiByStato(StatoProdotto.IN_ATTESA);
    }

    public List<Pacchetto> getPacchettiDaApprovare() {
        return pacchettoDAO.getPacchettiByStato(StatoProdotto.IN_ATTESA);
    }

    public boolean approvaProdotto(Prodotto prodotto) {
        boolean success = prodottoDAO.aggiornaStatoProdotto(prodotto, StatoProdotto.APPROVATO);
        if (success) {
            ProdottoNotifier.getInstance().notificaTutti(prodotto, "APPROVATO");
        }
        return success;
    }

    public boolean rifiutaProdotto(Prodotto prodotto, String commento) {
        if (commento == null) commento = "";

        boolean success = prodottoDAO.aggiornaStatoECommentoProdotto(prodotto, StatoProdotto.RIFIUTATO, commento);
        if (success) {
            ProdottoNotifier.getInstance().notificaTutti(prodotto, "RIFIUTATO");
        }
        return success;
    }

    public boolean approvaPacchetto(Pacchetto pacchetto) {
        boolean success = pacchettoDAO.aggiornaStatoPacchetto(pacchetto, StatoProdotto.APPROVATO);
        if (success) {
            PacchettoNotifier.getInstance().notificaTutti(pacchetto, "APPROVATO");
        }
        return success;
    }

    public boolean rifiutaPacchetto(Pacchetto pacchetto, String commento) {
        if (commento == null) commento = "";

        boolean success = pacchettoDAO.aggiornaStatoECommentoPacchetto(pacchetto, StatoProdotto.RIFIUTATO, commento);
        if (success) {
            PacchettoNotifier.getInstance().notificaTutti(pacchetto, "RIFIUTATO");
        }
        return success;
    }
}
