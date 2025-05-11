package unicam.filiera.controller;

import unicam.filiera.dao.*;
import unicam.filiera.model.*;

import java.util.*;
import java.util.function.Consumer;

public class MarketplaceController {

    private final ProdottoDAO prodottoDAO;
    private final PacchettoDAO pacchettoDAO;
    private final FieraDAO fieraDAO;
    private final VisitaInvitoDAO visitaDAO;
    private final List<Consumer<List<Object>>> osservatori = new ArrayList<>();

    public MarketplaceController() {
        this.prodottoDAO = JdbcProdottoDAO.getInstance();
        this.pacchettoDAO = JdbcPacchettoDAO.getInstance();
        this.fieraDAO = JdbcFieraDAO.getInstance();
        this.visitaDAO = JdbcVisitaInvitoDAO.getInstance();
    }

    /**
     * Restituisce tutti gli elementi (prodotti, pacchetti, fiere, visite)
     * in stato “vendibile” per il marketplace generico.
     */
    public List<Object> ottieniElementiMarketplace() {
        List<Object> out = new ArrayList<>();
        out.addAll(prodottoDAO.findByStato(StatoProdotto.APPROVATO));
        out.addAll(pacchettoDAO.findByStato(StatoProdotto.APPROVATO));
        out.addAll(fieraDAO.findByStato(StatoEvento.PUBBLICATA));
        out.addAll(visitaDAO.findByStato(StatoEvento.PUBBLICATA));
        return out;
    }

    public void registraOsservatore(Consumer<List<Object>> o) {
        osservatori.add(o);
    }

    public void notificaOsservatori() {
        List<Object> lista = ottieniElementiMarketplace();
        osservatori.forEach(o -> o.accept(lista));
    }

    public Object trovaElemento(String displayName) {
        for (Object obj : ottieniElementiMarketplace()) {
            if (labelDi(obj).equals(displayName)) return obj;
        }
        return null;
    }

    public static String labelDi(Object obj) {
        if (obj instanceof Prodotto p) return "[PR] " + p.getNome();
        if (obj instanceof Pacchetto k) return "[PK] " + k.getNome();
        if (obj instanceof Fiera fe) return "[FE] " + fe.getDescrizione()
                + " (" + fe.getDataInizio()
                + "→" + fe.getDataFine() + ")";
        if (obj instanceof VisitaInvito v) return "[VI] " + v.getDescrizione()
                + " (" + v.getDataInizio()
                + "→" + v.getDataFine() + ")";
        return obj.toString();
    }


}
