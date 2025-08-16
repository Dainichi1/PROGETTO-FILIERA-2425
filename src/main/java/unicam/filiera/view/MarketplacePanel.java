package unicam.filiera.view;

import unicam.filiera.controller.*;
import unicam.filiera.model.*;
import unicam.filiera.model.observer.OsservatorePacchetto;
import unicam.filiera.model.observer.OsservatoreProdotto;
import unicam.filiera.model.observer.OsservatoreFiera;
import unicam.filiera.model.observer.OsservatoreVisitaInvito;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.observer.OsservatoreProdottoTrasformato;
import unicam.filiera.model.observer.ProdottoTrasformatoNotifier;


import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * View del marketplace: mostra Prodotti APPROVATI, Pacchetti APPROVATI, Fiere PUBBLICATE e VisiteInvito PUBBLICATE
 */
public class MarketplacePanel extends JPanel
        implements OsservatoreProdotto, OsservatorePacchetto, OsservatoreFiera, OsservatoreVisitaInvito, OsservatoreProdottoTrasformato {


    /* ---------------- UI ---------------- */
    private final DefaultListModel<String> modello = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modello);
    private final JTextArea dettagli = new JTextArea();
    private final JButton btnBack = new JButton("Torna alla Home");

    /* ---------------- MVC ---------------- */
    private final MarketplaceController ctrl;
    private final JFrame mainFrame;

    public MarketplacePanel(JFrame parent, MarketplaceController ctrl) {
        super(new BorderLayout());
        this.mainFrame = parent;
        this.ctrl = ctrl;

        // Layout
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(lista), BorderLayout.WEST);

        dettagli.setEditable(false);
        add(new JScrollPane(dettagli), BorderLayout.CENTER);

        add(btnBack, BorderLayout.SOUTH);
        btnBack.addActionListener(e -> {
            if (mainFrame instanceof MainWindow w) w.tornaAllaHome();
        });

        // Quando cambio selezione, mostro dettagli
        lista.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String sel = lista.getSelectedValue();
            if (sel == null) return;
            Object elem = ctrl.trovaElemento(sel);
            mostraDettagli(elem);
        });

        // Observer pattern: mi registro per tutti e quattro i tipi
        ctrl.registraOsservatore(this::popolaLista);                     // marketplace controller (aggiorna lista)
        ObserverManagerProdotto.registraOsservatore(this);                    // prodotti
        ObserverManagerPacchetto.registraOsservatore(this);              // pacchetti
        ObserverManagerFiera.registraOsservatore(this);                  // fiere
        ObserverManagerVisitaInvito.registraOsservatore(this);   // visite su invito
        ProdottoTrasformatoNotifier.getInstance().registraOsservatore(this);

        // Carica iniziale
        popolaLista(ctrl.ottieniElementiMarketplace());
    }

    // Ricarica la JList
    private void popolaLista(List<Object> elementi) {
        modello.clear();
        for (Object o : elementi) {
            modello.addElement(MarketplaceController.labelDi(o));
        }
    }

    // Mostra i dettagli a seconda del tipo
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
                    String.join(", ", p.getFoto())
            ));
        } else if (obj instanceof Pacchetto k) {
            dettagli.setText("""
                    [PACCHETTO]
                    Nome: %s
                    Descrizione: %s
                    Prezzo Totale: %.2f €
                    Quantità: %d
                    Indirizzo: %s
                    Prodotti inclusi: %d
                    Certificati: %s
                    Foto: %s
                    """.formatted(
                    k.getNome(), k.getDescrizione(), k.getPrezzoTotale(),
                    k.getQuantita(),
                    k.getIndirizzo(), k.getProdotti().size(),
                    String.join(", ", k.getCertificati()),
                    String.join(", ", k.getFoto())
            ));
        } else if (obj instanceof Fiera f) {
            dettagli.setText("""
                    [FIERA]
                    Descrizione: %s
                    Date: %s → %s
                    Prezzo: %.2f €
                    Indirizzo: %s
                    Min. partecipanti: %d
                    Stato: %s
                    """.formatted(
                    f.getDescrizione(),
                    f.getDataInizio(), f.getDataFine(),
                    f.getPrezzo(),
                    f.getIndirizzo(),
                    f.getNumeroMinPartecipanti(),
                    f.getStato()
            ));
        } else if (obj instanceof VisitaInvito vi) {
            dettagli.setText("""
                    [VISITA INVITO]
                    Descrizione: %s
                    Date: %s → %s
                    Prezzo: %.2f €
                    Indirizzo: %s
                    Min. partecipanti: %d
                    Destinatari: %s
                    Stato: %s
                    """.formatted(
                    vi.getDescrizione(),
                    vi.getDataInizio(), vi.getDataFine(),
                    vi.getPrezzo(),
                    vi.getIndirizzo(),
                    vi.getNumeroMinPartecipanti(),
                    String.join(", ", vi.getDestinatari()),
                    vi.getStato()
            ));
        } else if (obj instanceof ProdottoTrasformato pt) {
            dettagli.setText("""
                    [PRODOTTO TRASFORMATO]
                    Nome: %s
                    Descrizione: %s
                    Prezzo: %.2f €
                    Indirizzo: %s
                    Quantità: %d
                    Certificati: %s
                    Foto: %s
                    Fasi produzione: %s
                    """.formatted(
                    pt.getNome(), pt.getDescrizione(), pt.getPrezzo(),
                    pt.getIndirizzo(), pt.getQuantita(),
                    String.join(", ", pt.getCertificati()),
                    String.join(", ", pt.getFoto()),
                    pt.fasiProduzioneAsString()
            ));


        } else {
            dettagli.setText("Selezione non riconosciuta.");
        }
    }

    // Callback Observer per Prodotto
    @Override
    public void notifica(Prodotto p, String evento) {
        if ("APPROVATO".equals(evento)) {
            SwingUtilities.invokeLater(() -> popolaLista(ctrl.ottieniElementiMarketplace()));
        }
    }

    // Callback Observer per Pacchetto
    @Override
    public void notifica(Pacchetto k, String ev) {
        if ("APPROVATO".equals(ev)) {
            SwingUtilities.invokeLater(() -> popolaLista(ctrl.ottieniElementiMarketplace()));
        }
    }

    // Callback Observer per Fiera
    @Override
    public void notifica(Fiera f, String evento) {
        if ("FIERA_PUBBLICATA".equals(evento) || "NUOVA_FIERA".equals(evento)) {
            SwingUtilities.invokeLater(() -> popolaLista(ctrl.ottieniElementiMarketplace()));
        }
    }

    // Callback Observer per VisitaInvito
    @Override
    public void notifica(VisitaInvito vi, String evento) {
        if ("VISITA_INVITO_PUBBLICATA".equals(evento)) {
            SwingUtilities.invokeLater(() -> popolaLista(ctrl.ottieniElementiMarketplace()));
        }
    }

    @Override
    public void notifica(ProdottoTrasformato pt, String evento) {
        if ("APPROVATO".equals(evento)) {
            SwingUtilities.invokeLater(() -> popolaLista(ctrl.ottieniElementiMarketplace()));
        }
    }


    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManagerProdotto.rimuoviOsservatore(this);
        ObserverManagerPacchetto.rimuoviOsservatore(this);
        ObserverManagerFiera.rimuoviOsservatore(this);
        ObserverManagerVisitaInvito.rimuoviOsservatore(this);
        ProdottoTrasformatoNotifier.getInstance().rimuoviOsservatore(this);

    }

    public JButton getBtnIndietro() {
        return btnBack;
    }
}
