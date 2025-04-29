/* ==================================================================== */
/*  MarketplaceController.java                                          */
/* ==================================================================== */
package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.dao.PacchettoDAO;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.util.*;
import java.util.function.Consumer;

public class MarketplaceController {

    /* --------------------- DAO & cache --------------------- */
    private final ProdottoDAO  prodottoDAO  = new ProdottoDAO();
    private final PacchettoDAO pacchettoDAO = new PacchettoDAO();

    /* tipo «Object» perché può essere Prodotto o Pacchetto              */
    private final List<Consumer<List<Object>>> osservatori = new ArrayList<>();

    /* ---------------------- API pubbliche ------------------ */
    public List<Object> ottieniElementiMarketplace() {
        List<Object> out = new ArrayList<>();
        // prodotti approvati
        out.addAll(prodottoDAO.getProdottiByStato(StatoProdotto.APPROVATO));
        // pacchetti approvati
        out.addAll(pacchettoDAO.getPacchettiByStato(StatoProdotto.APPROVATO));
        return out;
    }

    /**Registrazione callback di view / altri componenti*/
    public void registraOsservatore(Consumer<List<Object>> o) {
        osservatori.add(o);
    }

    /**Invocato da chi cambia i dati (o all’avvio)*/
    public void notificaOsservatori() {
        List<Object> lista = ottieniElementiMarketplace();
        osservatori.forEach(o -> o.accept(lista));
    }

    /* -------- helper: restituisce l’oggetto dato il “display name” ---- */
    public Object trovaElemento(String displayName) {
        for (Object obj : ottieniElementiMarketplace()) {
            String label = labelDi(obj);
            if (label.equals(displayName)) return obj;
        }
        return null;
    }

    /* label uniforme “[PK] …” / “[PR] …”                                */
    public static String labelDi(Object obj) {
        return (obj instanceof Pacchetto p)
                ? "[PK] " + p.getNome()
                : "[PR] " + ((Prodotto) obj).getNome();
    }
}
