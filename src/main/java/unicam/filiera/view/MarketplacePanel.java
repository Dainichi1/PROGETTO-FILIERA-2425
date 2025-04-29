/* ==================================================================== */
/*  MarketplacePanel.java                                               */
/* ==================================================================== */
package unicam.filiera.view;

import unicam.filiera.controller.MarketplaceController;
import unicam.filiera.controller.ObserverManager;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.observer.OsservatorePacchetto;
import unicam.filiera.model.observer.OsservatoreProdotto;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** View del marketplace: mostra Prodotti APPROVATI e Pacchetti APPROVATI */
public class MarketplacePanel extends JPanel
        implements OsservatoreProdotto, OsservatorePacchetto {

    /* ---------------- UI ---------------- */
    private final DefaultListModel<String> modello = new DefaultListModel<>();
    private final JList<String>            lista   = new JList<>(modello);
    private final JTextArea                dettagli= new JTextArea();
    private final JButton                  btnBack = new JButton("Torna alla Home");

    /* ---------------- MVC ---------------- */
    private final MarketplaceController ctrl;
    private final JFrame mainFrame;

    public MarketplacePanel(JFrame parent, MarketplaceController ctrl) {
        super(new BorderLayout());
        this.mainFrame = parent;
        this.ctrl      = ctrl;

        /* ---- layout ---- */
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(lista), BorderLayout.WEST);

        dettagli.setEditable(false);
        add(new JScrollPane(dettagli), BorderLayout.CENTER);

        add(btnBack, BorderLayout.SOUTH);
        btnBack.addActionListener(e -> {
            if (mainFrame instanceof MainWindow w) w.tornaAllaHome();
        });

        /* ---- listener lista ---- */
        lista.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String sel = lista.getSelectedValue();
            if (sel == null) return;

            Object elem = ctrl.trovaElemento(sel);
            mostraDettagli(elem);
        });

        /* ---- Observer pattern ---- */
        ctrl.registraOsservatore(this::popolaLista);
        ObserverManager.registraOsservatore(this);         // prodotti
        unicam.filiera.model.observer.PacchettoNotifier
                .getInstance().registraOsservatore(this);  // pacchetti

        /* ---- bootstrap ---- */
        popolaLista(ctrl.ottieniElementiMarketplace());
    }

    /* ================================================================= */
    /*  RENDERING LISTA + DETTAGLI                                       */
    /* ================================================================= */
    private void popolaLista(List<Object> elementi) {
        modello.clear();
        for (Object o : elementi) modello.addElement(MarketplaceController.labelDi(o));
    }

    private void mostraDettagli(Object obj) {
        if (obj instanceof Prodotto p) {
            dettagli.setText("""
                    [PRODOTTO]
                    Nome: %s
                    Descrizione: %s
                    Prezzo: %.2f €
                    Indirizzo: %s
                    Quantità: %d
                    Certificati: %s
                    Foto: %s
                    """.formatted(
                    p.getNome(), p.getDescrizione(), p.getPrezzo(),
                    p.getIndirizzo(), p.getQuantita(),
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto())));
        } else if (obj instanceof Pacchetto k) {
            dettagli.setText("""
                    [PACCHETTO]
                    Nome: %s
                    Descrizione: %s
                    Prezzo Totale: %.2f €
                    Indirizzo: %s
                    Prodotti inclusi: %d
                    Certificati: %s
                    Foto: %s
                    """.formatted(
                    k.getNome(), k.getDescrizione(), k.getPrezzoTotale(),
                    k.getIndirizzo(), k.getProdotti().size(),
                    String.join(", ", k.getCertificati()),
                    String.join(", ", k.getFoto())));
        } else {
            dettagli.setText("Selezione non riconosciuta.");
        }
    }

    /* ================================================================= */
    /*  CALLBACK OBSERVER (Prodotti & Pacchetti)                         */
    /* ================================================================= */
    @Override
    public void notifica(Prodotto p, String evento) {
        if ("APPROVATO".equals(evento)) SwingUtilities.invokeLater(() ->
                popolaLista(ctrl.ottieniElementiMarketplace()));
    }

    @Override
    public void notifica(unicam.filiera.model.Pacchetto k, String ev) {
        if ("APPROVATO".equals(ev)) SwingUtilities.invokeLater(() ->
                popolaLista(ctrl.ottieniElementiMarketplace()));
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManager.rimuoviOsservatore(this);
        unicam.filiera.model.observer.PacchettoNotifier
                .getInstance().rimuoviOsservatore(this);
    }

    public JButton getBtnIndietro() { return btnBack; }
}
