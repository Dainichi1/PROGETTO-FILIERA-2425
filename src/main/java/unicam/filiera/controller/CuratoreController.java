package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.dao.PacchettoDAO;
import unicam.filiera.dao.JdbcProdottoDAO;
import unicam.filiera.dao.JdbcPacchettoDAO;
import unicam.filiera.model.Item;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;
import unicam.filiera.model.observer.PacchettoNotifier;
import unicam.filiera.dao.ProdottoTrasformatoDAO;
import unicam.filiera.dao.JdbcProdottoTrasformatoDAO;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.observer.ProdottoTrasformatoNotifier;

import java.util.List;
import java.util.function.BiConsumer;

public class CuratoreController {

    private final ProdottoDAO prodottoDAO;
    private final PacchettoDAO pacchettoDAO;
    private final ProdottoTrasformatoDAO prodottoTrasformatoDAO;
    private final ProdottoTrasformatoNotifier prodottoTrasformatoNotifier;
    private final ProdottoNotifier prodottoNotifier;
    private final PacchettoNotifier pacchettoNotifier;

    public CuratoreController(ProdottoDAO prodottoDAO, PacchettoDAO pacchettoDAO, ProdottoTrasformatoDAO prodottoTrasformatoDAO) {
        this.prodottoDAO = prodottoDAO;
        this.pacchettoDAO = pacchettoDAO;
        this.prodottoTrasformatoDAO = prodottoTrasformatoDAO;
        this.prodottoNotifier = ProdottoNotifier.getInstance();
        this.pacchettoNotifier = PacchettoNotifier.getInstance();
        this.prodottoTrasformatoNotifier = ProdottoTrasformatoNotifier.getInstance();
    }

    public CuratoreController() {
        this(
                JdbcProdottoDAO.getInstance(),
                JdbcPacchettoDAO.getInstance(),
                JdbcProdottoTrasformatoDAO.getInstance()
        );
    }


    public List<Prodotto> getProdottiDaApprovare() {
        return prodottoDAO.findByStato(StatoProdotto.IN_ATTESA);
    }

    public List<Pacchetto> getPacchettiDaApprovare() {
        return pacchettoDAO.findByStato(StatoProdotto.IN_ATTESA);
    }

    public List<ProdottoTrasformato> getProdottiTrasformatiDaApprovare() {
        return prodottoTrasformatoDAO.findByStato(StatoProdotto.IN_ATTESA);
    }


    public void valutaItem(Item item, boolean approva, String commento, BiConsumer<Boolean, String> callback) {
        boolean success = false;
        String nomeElemento = item instanceof Pacchetto ? "[PAC] " + item.getNome() :
                item instanceof ProdottoTrasformato ? "[TRASF] " + item.getNome() :
                        item.getNome();

        try {
            item.setStato(approva ? StatoProdotto.APPROVATO : StatoProdotto.RIFIUTATO);
            item.setCommento(approva ? null : (commento != null ? commento : ""));

            if (item instanceof Prodotto p) {
                success = prodottoDAO.update(p);
                if (success)
                    prodottoNotifier.notificaTutti(p, approva ? "APPROVATO" : "RIFIUTATO");
            } else if (item instanceof Pacchetto k) {
                success = pacchettoDAO.update(k);
                if (success)
                    pacchettoNotifier.notificaTutti(k, approva ? "APPROVATO" : "RIFIUTATO");
            } else if (item instanceof ProdottoTrasformato pt) {
                System.out.println("[CURATORE] Sto per notificare evento su prodotto trasformato: " + pt.getNome() + " creato da " + pt.getCreatoDa());
                success = prodottoTrasformatoDAO.update(pt);
                if (success)
                    prodottoTrasformatoNotifier.notificaTutti(pt, approva ? "APPROVATO" : "RIFIUTATO");
            }

            String esito = success
                    ? (approva ? nomeElemento + " approvato." : nomeElemento + " rifiutato.")
                    : "Operazione fallita su " + nomeElemento;

            callback.accept(success, esito);

        } catch (Exception e) {
            callback.accept(false, "Errore: " + e.getMessage());
        }
    }


}
