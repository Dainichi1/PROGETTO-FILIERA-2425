package unicam.filiera.controller;

import unicam.filiera.dao.*;
import unicam.filiera.model.Fiera;
import unicam.filiera.model.VisitaInvito;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.model.StatoProdotto;


import java.util.*;
import java.util.function.Consumer;

public class MarketplaceController {

    private final ProdottoDAO  prodottoDAO;
    private final PacchettoDAO pacchettoDAO;
    private final FieraDAO     fieraDAO;
    private final VisitaInvitoDAO visitaDAO;
    private final List<Consumer<List<Object>>> osservatori = new ArrayList<>();

    public MarketplaceController() {
        // usa le implementazioni JDBC tramite singleton
        this.prodottoDAO  = JdbcProdottoDAO.getInstance();
        this.pacchettoDAO = JdbcPacchettoDAO.getInstance();
        this.fieraDAO     = JdbcFieraDAO.getInstance();
        this.visitaDAO    = JdbcVisitaInvitoDAO.getInstance();
    }

    public List<Object> ottieniElementiMarketplace() {
        List<Object> out = new ArrayList<>();
        // prodotti approvati
        out.addAll(prodottoDAO.findByStato(StatoProdotto.APPROVATO));
        // pacchetti approvati
        out.addAll(pacchettoDAO.findByStato(StatoProdotto.APPROVATO));
        // fiere pubblicate
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
            String label = labelDi(obj);
            if (label.equals(displayName)) return obj;
        }
        return null;
    }

    public static String labelDi(Object obj) {
        if (obj instanceof Prodotto p) {
            return "[PR] " + p.getNome();
        }
        if (obj instanceof Pacchetto pk) {
            return "[PK] " + pk.getNome();
        }
        if (obj instanceof Fiera fe) {
            return "[FE] " + fe.getDescrizione()
                    + " (" + fe.getDataInizio() + "→" + fe.getDataFine() + ")";
        }
        if (obj instanceof VisitaInvito vi) {
            return "[VI] " + vi.getDescrizione()
                    + " (" + vi.getDataInizio() + "→" + vi.getDataFine() + ")";
        }
        return obj.toString();
    }
}
